package discordbot.bot.command

import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class PingCommand : SlashCommand {
    private val logger = LoggerFactory.getLogger(PingCommand::class.java)

    override val name = "ping"

    override fun register(kord: dev.kord.core.Kord) {
        logger.info("Registering ping command...")

        kord.on<GuildChatInputCommandInteractionCreateEvent> {
            if (interaction.command.rootName != name) return@on
            handle(this)
        }
    }

    override suspend fun handle(event: GuildChatInputCommandInteractionCreateEvent) {
        logger.info("Received ping command!")

        event.interaction.respondPublic {
            content = "Pong!"
        }
    }
}