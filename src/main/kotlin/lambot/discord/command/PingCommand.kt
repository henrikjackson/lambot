package lambot.discord.command

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import lambot.config.DiscordProperties
import org.springframework.stereotype.Component

@Component
class PingCommand(private val discordProperties: DiscordProperties) : SlashCommand {
    override val name = "ping"
    override val description = "Check if the bot is alive"
    override val requiredRoleIds: Set<String> get() = discordProperties.allowedRoleIds.toSet()

    override suspend fun register(kord: Kord) {}

    override suspend fun handle(event: GuildChatInputCommandInteractionCreateEvent) {
        event.interaction.respondEphemeral {
            content = "Pong!"
        }
    }
}
