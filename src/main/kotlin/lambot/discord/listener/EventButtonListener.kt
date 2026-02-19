package lambot.discord.listener

import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.on
import lambot.discord.event.EventService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class EventButtonListener(
    private val eventService: EventService
) : DiscordListener {
    private val logger = LoggerFactory.getLogger(EventButtonListener::class.java)

    override fun register(kord: dev.kord.core.Kord) {
        kord.on<ButtonInteractionCreateEvent> {
            logger.info("Received button interaction: ${interaction.componentId}")

            logger.info("Interaction appId = ${interaction.applicationId}")

            val messageId = interaction.message.id
            val userId = interaction.user.id

            when (interaction.componentId) {
                "event:yes" -> eventService.respondYes(messageId, userId)
                "event:no" -> eventService.respondNo(messageId, userId)
                else -> return@on
            }

            val state = eventService.get(messageId) ?: return@on

            interaction.respondPublic { content = "Response recorded!" }

            interaction.message.edit {
                content = buildMessage(
                    yes = state.yes.map { "<@$it>" },
                    no = state.no.map { "<@$it>" }
                )
            }

        }
    }

    private fun buildMessage(
        yes: List<String>,
        no: List<String>
    ): String =
        """
        📅 **New Event**

        ✅ **Yes (${yes.size})**
        ${yes.joinToString("\n").ifBlank { "_No responses yet_" }}

        ❌ **No (${no.size})**
        ${no.joinToString("\n").ifBlank { "_No responses yet_" }}
        """.trimIndent()
}