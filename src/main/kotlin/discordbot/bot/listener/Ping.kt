package discordbot.bot.listener

import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class Ping : DiscordListener {
    private val logger = LoggerFactory.getLogger(Ping::class.java)

    override fun register(kord: dev.kord.core.Kord) {
        kord.on<MessageCreateEvent> {
            logger.info("Received message: ${message.content}")

            if (message.author?.isBot != false) return@on
            if (message.content != "!ping") return@on

            message.channel.createMessage("Pong!")
        }
    }
}