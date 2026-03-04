package lambot.discord.command

import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import org.springframework.stereotype.Component

@Component
class PingCommand : SlashCommand {
    override val name = "ping"
    override val description = "Check if the bot is alive"

    override suspend fun register(kord: dev.kord.core.Kord) {}

    override suspend fun handle(event: GuildChatInputCommandInteractionCreateEvent) {
        event.interaction.respondPublic {
            content = "Pong!"
        }
    }
}
