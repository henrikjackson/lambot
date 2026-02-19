package lambot.discord.listener

import dev.kord.core.Kord
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.on
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import lambot.discord.events.EventSignupService
import lambot.discord.events.eventMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class EventButtonListener(
    private val eventSignupService: EventSignupService
) : DiscordListener {

    private val logger = LoggerFactory.getLogger(EventButtonListener::class.java)

    override fun register(kord: Kord) {
        kord.on<ButtonInteractionCreateEvent> {
            val componentId = interaction.componentId

            if (!componentId.startsWith("event:")) return@on

            val parts = componentId.split(":")
            val action = parts[1]
            val eventId = UUID.fromString(parts[2])
            val interactinUserID = interaction.user.id.value.toLong()

            logger.info("Button clicked: $action on event $eventId by $interactinUserID")

            val updatedEvent = when (action) {
                "yes" -> eventSignupService.attend(eventId, interactinUserID)
                "no" -> eventSignupService.decline(eventId, interactinUserID)
                else -> return@on
            }.getOrElse {
                interaction.respondEphemeral {
                    content = it.message ?: "Failed to update event"
                }
                return@on
            }

            interaction.message.edit {
                eventMessage(updatedEvent)
            }

            interaction.respondEphemeral {
                content = "Your response has been recorded 👍"
            }
        }
    }
}
