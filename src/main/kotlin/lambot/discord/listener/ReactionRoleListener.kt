package lambot.discord.listener

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.event.message.ReactionRemoveEvent
import dev.kord.core.on
import kotlinx.coroutines.flow.firstOrNull
import lambot.config.DiscordProperties
import lambot.config.FeaturesProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ReactionRoleListener(
    private val properties: DiscordProperties,
    private val features: FeaturesProperties
) : DiscordListener {

    private val logger = LoggerFactory.getLogger(ReactionRoleListener::class.java)

    private val marker = "**Rolleoppdrag**"

    @Volatile
    private var roleMessageId: Snowflake? = null

    override fun register(kord: Kord) {
        if (!features.reactionRoles) {
            logger.info("Reaction roles feature is disabled")
            return
        }
        if (properties.roleMessageChannelId.isBlank() || properties.roleAssignments.isEmpty()) {
            logger.info("Role assignments not configured, skipping ReactionRoleListener")
            return
        }

        kord.on<ReadyEvent> {
            roleMessageId = findOrCreateRoleMessage(kord)
            logger.info("Role message ID set to: $roleMessageId")
        }

        kord.on<ReactionAddEvent> {
            val msgId = roleMessageId ?: return@on
            if (messageId != msgId || userId == kord.selfId) return@on

            val emojiName = (emoji as? ReactionEmoji.Unicode)?.name ?: return@on
            val assignment = properties.roleAssignments.find { it.emoji == emojiName } ?: return@on
            val guildId = this.guildId ?: return@on

            try {
                val member = kord.getGuildOrNull(guildId)?.getMemberOrNull(userId) ?: return@on
                member.addRole(Snowflake(assignment.roleId))
                logger.info("Added role ${assignment.roleId} to user $userId")
            } catch (e: Exception) {
                logger.error("Failed to add role ${assignment.roleId} to user $userId: ${e.message}", e)
            }
        }

        kord.on<ReactionRemoveEvent> {
            val msgId = roleMessageId ?: return@on
            if (messageId != msgId) return@on

            val emojiName = (emoji as? ReactionEmoji.Unicode)?.name ?: return@on
            val assignment = properties.roleAssignments.find { it.emoji == emojiName } ?: return@on
            val guildId = this.guildId ?: return@on

            try {
                val member = kord.getGuildOrNull(guildId)?.getMemberOrNull(userId) ?: return@on
                member.removeRole(Snowflake(assignment.roleId))
                logger.info("Removed role ${assignment.roleId} from user $userId")
            } catch (e: Exception) {
                logger.error("Failed to remove role ${assignment.roleId} from user $userId: ${e.message}", e)
            }
        }
    }

    private suspend fun findOrCreateRoleMessage(kord: Kord): Snowflake? {
        val channelId = Snowflake(properties.roleMessageChannelId)
        val channel = kord.getChannelOf<TextChannel>(channelId) ?: run {
            logger.error("Role message channel not found: ${properties.roleMessageChannelId}")
            return null
        }
        val guild = kord.getGuildOrNull(Snowflake(properties.guildId)) ?: return null

        val existing = channel.getMessagesBefore(Snowflake.max, 50)
            .firstOrNull { it.author?.id == kord.selfId && it.content.startsWith(marker) }

        if (existing != null) {
            logger.info("Reusing existing role message: ${existing.id}")
            return existing.id
        }

        val roleLines = properties.roleAssignments.mapNotNull { assignment ->
            val roleName = guild.getRoleOrNull(Snowflake(assignment.roleId))?.name ?: return@mapNotNull null
            "${assignment.emoji} – $roleName"
        }

        val content = buildString {
            appendLine(marker)
            appendLine("React med en emoji for å få en rolle:")
            appendLine()
            roleLines.forEach { appendLine(it) }
            appendLine()
            append("_Fjern reaksjonen din for å miste rollen._")
        }

        val message = channel.createMessage(content)
        properties.roleAssignments.forEach { assignment ->
            message.addReaction(ReactionEmoji.Unicode(assignment.emoji))
        }
        logger.info("Posted new role message: ${message.id}")
        return message.id
    }
}
