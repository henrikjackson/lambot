package lambot.discord.command

import dev.kord.core.Kord
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import lambot.discord.listener.DiscordListener
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SlashCommandDispatcher(
    private val commands: List<SlashCommand>
) : DiscordListener {

    private val logger = LoggerFactory.getLogger(SlashCommandDispatcher::class.java)

    override fun register(kord: Kord) {
        kord.on<GuildChatInputCommandInteractionCreateEvent> {
            val rootName = interaction.command.rootName

            logger.info("Received slash command: $rootName")

            val command = commands.find { it.name == rootName } ?: return@on

            command.handle(this)
        }
    }

}