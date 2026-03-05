package lambot.discord.command

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import lambot.config.DiscordProperties
import lambot.config.FeaturesProperties
import lambot.discord.listener.DiscordListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SlashCommandDispatcher(
    private val commands: List<SlashCommand>,
    private val discordProperties: DiscordProperties,
    private val features: FeaturesProperties
) : DiscordListener {

    private val logger = LoggerFactory.getLogger(SlashCommandDispatcher::class.java)

    override fun register(kord: Kord) {
        kord.on<GuildChatInputCommandInteractionCreateEvent> {
            val rootName = interaction.command.rootName
            logger.info("Received slash command: $rootName")

            val command = commands.find { it.name == rootName } ?: return@on

            val memberRoleIds = interaction.user.asMember(interaction.guildId).roleIds.map { it.toString() }.toSet()
            val hasAllowedRole = discordProperties.allowedRoleIds.toSet().intersect(memberRoleIds).isNotEmpty()

            if (!features.commands && !hasAllowedRole) {
                interaction.respondEphemeral { content = "Kommandoer er midlertidig deaktivert." }
                return@on
            }

            if (command.requiredRoleIds.isNotEmpty() && memberRoleIds.intersect(command.requiredRoleIds).isEmpty()) {
                interaction.respondEphemeral { content = "Du har ikke tilgang til denne kommandoen." }
                return@on
            }

            command.handle(this)
        }
    }
}
