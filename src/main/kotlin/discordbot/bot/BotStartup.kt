package discordbot.bot

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.gateway.Intent
import dev.kord.gateway.Intents
import dev.kord.gateway.PrivilegedIntent
import discordbot.bot.command.SlashCommand
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
    private val listeners: List<DiscordListener>,
    private val commands: List<SlashCommand>
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

            val guildId = Snowflake("784044051791478796")

            listeners.forEach { listener ->
                listener.register(kord)
                logger.info("Registered listener: ${listener.javaClass.simpleName}")
            }

            commands.forEach { command ->
                kord.createGuildChatInputCommand(
                    guildId,
                    command.name,
                    "Command ${command.name} description"
                )
                command.register(kord)
            }

            kord.login {
                intents = Intents {
                    +Intent.GuildMessages
                    +Intent.MessageContent
                    +Intent.Guilds
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
