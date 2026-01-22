package discordbot.bot

import dev.kord.core.Kord
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import discordbot.bot.listener.DiscordListener
import discordbot.config.DiscordProperties
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class BotStartup(
    private val properties: DiscordProperties,
    private val listeners: List<DiscordListener>
) {
    private val logger = LoggerFactory.getLogger(BotStartup::class.java)

    private val supervisor = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + supervisor)

    private lateinit var kord: Kord
    private var botJob: Job? = null

    @OptIn(PrivilegedIntent::class)
    @PostConstruct
    fun startBot() {
        logger.info("Starting bot...")

        botJob = scope.launch {
            kord = Kord(properties.token)

            listeners.forEach { listener ->
                listener.register(kord)
                logger.info("Registered listener: ${listener.javaClass.simpleName}")
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

        botJob?.cancel()

        supervisor.cancel()
    }
}
