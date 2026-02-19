package lambot.discord.listener

import dev.kord.core.Kord
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.on
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import lambot.discord.events.EventSignupService
import lambot.discord.events.Role
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
            val parts = interaction.componentId.split(":")
            if (parts.first() != "event") return@on

            val role = when (parts[1]) {
                "tank" -> Role.TANK
                "healer" -> Role.HEALER
                "dps" -> Role.DPS
                "na" -> Role.NOT_ATTENDING
                else -> return@on
            }

            val eventId = UUID.fromString(parts[2])
            val userId = interaction.user.id.value.toLong()

            val updatedEvent = eventSignupService
                .signup(eventId, userId, role)
                .getOrElse {
                    interaction.respondEphemeral { content = "An error occurred: ${it.message}" }
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
