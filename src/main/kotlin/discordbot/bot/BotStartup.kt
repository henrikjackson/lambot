package discordbot.bot

import dev.kord.core.Kord
import dev.kord.core.event.message.MessageCreateEvent
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import discordbot.config.DiscordProperties
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class BotStartup(
    private val properties: DiscordProperties
) {
    private val logger = LoggerFactory.getLogger(BotStartup::class.java)

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    @OptIn(PrivilegedIntent::class)
    @PostConstruct
    fun startBot() {
        logger.info("Starting bot...")

        scope.launch {
            val kord = Kord(properties.token)

            kord.on<MessageCreateEvent> {
                logger.info("Received message: ${message.content}")

                if (message.author?.isBot != false) return@on
                if (message.content != "!ping") return@on

                message.channel.createMessage("Pong!")
            }

            kord.login {
                intents = Intents {
                    +Intent.GuildMessages
                    +Intent.MessageContent
                }
            }
        }
    }

    @PreDestroy
    fun shutdown() {
        logger.info("Shutting down bot...")
        job.cancel()
    }
}
