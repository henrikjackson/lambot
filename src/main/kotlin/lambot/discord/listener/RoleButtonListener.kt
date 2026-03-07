package lambot.discord.listener

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.GuildButtonInteractionCreateEvent
import dev.kord.core.event.interaction.GuildSelectMenuInteractionCreateEvent
import dev.kord.core.on
import dev.kord.common.entity.DiscordPartialEmoji
import dev.kord.rest.builder.component.SelectOptionBuilder
import kotlinx.coroutines.flow.toList
import dev.kord.rest.builder.message.actionRow
import lambot.config.DiscordProperties
import lambot.config.FeaturesProperties
import lambot.discord.role.MythicRosterService
import lambot.discord.role.RoleMessageService
import lambot.discord.role.UserDataService
import lambot.discord.role.WowClasses
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class RoleButtonListener(
    private val properties: DiscordProperties,
    private val features: FeaturesProperties,
    private val roleMessageService: RoleMessageService,
    private val mythicRosterService: MythicRosterService,
    private val userDataService: UserDataService
) : DiscordListener {

    private val logger = LoggerFactory.getLogger(RoleButtonListener::class.java)

    private val cooldowns = ConcurrentHashMap<Snowflake, Long>()
    private val cooldownMs = 5_000L

    override fun register(kord: Kord) {
        if (!features.reactionRoles) {
            logger.info("Reaction roles feature is disabled")
            return
        }

        kord.on<ReadyEvent> {
            roleMessageService.initialize(kord)
            mythicRosterService.initialize(kord)
        }

        kord.on<GuildButtonInteractionCreateEvent> {
            val msgId = roleMessageService.roleMessageId ?: return@on
            if (interaction.message.id != msgId) return@on

            val now = System.currentTimeMillis()
            val last = cooldowns[interaction.user.id] ?: 0L
            if (now - last < cooldownMs) {
                logger.info("Rate limited button click from ${interaction.user.id}")
                return@on
            }
            cooldowns[interaction.user.id] = now

            val componentId = interaction.componentId
            when {
                componentId == "role:remove" -> handleRemove(kord, this)
                componentId.startsWith("role:") -> handleRoleButton(kord, this, componentId.removePrefix("role:"))
            }
        }

        kord.on<GuildSelectMenuInteractionCreateEvent> {
            val componentId = interaction.componentId
            when {
                componentId == "mythic-class" -> handleClassSelect(kord, this)
                componentId.startsWith("mythic-spec:") -> handleSpecSelect(kord, this, componentId.removePrefix("mythic-spec:"))
            }
        }
    }

    private suspend fun handleRoleButton(kord: Kord, event: GuildButtonInteractionCreateEvent, roleId: String) {
        val assignment = properties.roleAssignments.find { it.roleId == roleId } ?: return

        if (assignment.mythic) {
            event.interaction.respondEphemeral {
                content = "Velg klassen din:"
                actionRow {
                    stringSelect("mythic-class") {
                        placeholder = "Velg klasse..."
                        WowClasses.specs.keys.forEach { className ->
                            options.add(SelectOptionBuilder(className, WowClasses.keyFor(className)))
                        }
                    }
                }
            }
        } else {
            val response = event.interaction.respondEphemeral { content = "Tildeler rolle..." }
            assignRole(kord, event, roleId, assignment.label, response)
        }
    }

    private suspend fun handleClassSelect(kord: Kord, event: GuildSelectMenuInteractionCreateEvent) {
        val classKey = event.interaction.values.firstOrNull() ?: return
        val className = WowClasses.fromKey(classKey) ?: return
        val specs = WowClasses.specsFor(classKey)

        val guild = kord.getGuildOrNull(event.interaction.guildId)
        val emojiMap = guild?.emojis?.toList()?.associateBy { it.name } ?: emptyMap()

        event.interaction.respondEphemeral {
            content = "Velg spec for **$className**:"
            actionRow {
                stringSelect("mythic-spec:$classKey") {
                    placeholder = "Velg spec..."
                    specs.forEach { spec ->
                        val specKey = spec.lowercase().replace(" ", "-")
                        options.add(SelectOptionBuilder(spec, specKey).apply {
                            val emojiName = WowClasses.specEmojiName(className, spec)
                            val guildEmoji = emojiMap[emojiName]
                            if (guildEmoji != null) {
                                emoji = DiscordPartialEmoji(id = guildEmoji.id, name = guildEmoji.name)
                            }
                        })
                    }
                }
            }
        }
    }

    private suspend fun handleSpecSelect(kord: Kord, event: GuildSelectMenuInteractionCreateEvent, classKey: String) {
        val specKey = event.interaction.values.firstOrNull() ?: return
        val className = WowClasses.fromKey(classKey) ?: return
        val specName = WowClasses.specsFor(classKey).firstOrNull { it.lowercase().replace(" ", "-") == specKey } ?: return
        val mythicAssignment = properties.roleAssignments.find { it.mythic } ?: return

        val response = event.interaction.respondEphemeral { content = "Registrerer..." }

        try {
            val guild = kord.getGuildOrNull(event.interaction.guildId) ?: return
            val member = guild.getMemberOrNull(event.interaction.user.id) ?: return

            removeOtherRoles(kord, member, mythicAssignment.roleId)
            member.addRole(Snowflake(mythicAssignment.roleId))
            userDataService.save(event.interaction.user.id.toString(), className, specName)
            mythicRosterService.update(kord)

            logger.info("Assigned Mythic role to ${member.effectiveName} as $className $specName")
            response.edit { content = "Du er nå registrert som **$specName $className** og har fått **${mythicAssignment.label}**-rollen!" }
        } catch (e: Exception) {
            logger.error("Failed to assign Mythic role: ${e.message}", e)
            response.edit { content = "Noe gikk galt: ${e.message}" }
        }
    }

    private suspend fun handleRemove(kord: Kord, event: GuildButtonInteractionCreateEvent) {
        val response = event.interaction.respondEphemeral { content = "Fjerner rolle..." }
        try {
            val guild = kord.getGuildOrNull(event.interaction.guildId) ?: return
            val member = guild.getMemberOrNull(event.interaction.user.id) ?: return

            val managedRoleIds = properties.roleAssignments.map { Snowflake(it.roleId) }.toSet()
            managedRoleIds.filter { it in member.roleIds }.forEach { member.removeRole(it) }
            userDataService.remove(event.interaction.user.id.toString())
            mythicRosterService.update(kord)

            logger.info("Removed all managed roles from ${member.effectiveName}")
            response.edit { content = "Rollen din er fjernet." }
        } catch (e: Exception) {
            logger.error("Failed to remove roles: ${e.message}", e)
            response.edit { content = "Noe gikk galt: ${e.message}" }
        }
    }

    private suspend fun assignRole(
        kord: Kord,
        event: GuildButtonInteractionCreateEvent,
        roleId: String,
        label: String,
        response: dev.kord.core.behavior.interaction.response.EphemeralMessageInteractionResponseBehavior
    ) {
        try {
            val guild = kord.getGuildOrNull(event.interaction.guildId) ?: return
            val member = guild.getMemberOrNull(event.interaction.user.id) ?: return

            removeOtherRoles(kord, member, roleId)
            member.addRole(Snowflake(roleId))
            userDataService.remove(event.interaction.user.id.toString())
            mythicRosterService.update(kord)

            logger.info("Assigned $label role to ${member.effectiveName}")
            response.edit { content = "Du har fått **$label**-rollen!" }
        } catch (e: Exception) {
            logger.error("Failed to assign role $roleId: ${e.message}", e)
            response.edit { content = "Noe gikk galt: ${e.message}" }
        }
    }

    private suspend fun removeOtherRoles(kord: Kord, member: dev.kord.core.entity.Member, keepRoleId: String) {
        properties.roleAssignments
            .filter { it.roleId != keepRoleId && Snowflake(it.roleId) in member.roleIds }
            .forEach { other ->
                member.removeRole(Snowflake(other.roleId))
                logger.info("Removed conflicting role ${other.label} from ${member.effectiveName}")
            }
    }
}
