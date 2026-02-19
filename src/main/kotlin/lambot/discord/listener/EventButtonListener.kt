package lambot.discord.listener

import dev.kord.core.Kord
import dev.kord.core.event.interaction.ButtonInteractionCreateEvent
import dev.kord.core.on
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.SelectMenuInteractionCreateEvent
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.rest.builder.component.option
import lambot.discord.events.EventSignupService
import lambot.raid.Role
import lambot.discord.events.eventMessage
import lambot.raid.RaidProfileService
import lambot.raid.WowClass
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class EventButtonListener(
    private val eventSignupService: EventSignupService,
    private val raidProfileService: RaidProfileService
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

            if (role == Role.NOT_ATTENDING) {
                val updated = eventSignupService
                    .signup(eventId, userId, role, null)
                    .getOrElse {
                        interaction.respondEphemeral { content = "An error occurred: ${it.message}" }
                        return@on }

                val messageId = updated.messageId ?: return@on
                val channel = interaction.channel

                channel.getMessage(messageId).edit {
                    eventMessage(updated)
                }

                interaction.respondEphemeral { content = "You have unsubscribed from this event." }
                return@on
            }

            val existingClass = raidProfileService.getClassForRole(userId, role)

            if (existingClass == null) {
                // Ask for class (ephemeral select menu)
                interaction.respondEphemeral {
                    content = "Choose your **${role.displayName}** class:"
                    components = mutableListOf(
                        ActionRowBuilder().apply {
                            stringSelect(
                                customId = "class:${role.name}:${eventId}"
                            ) {
                                WowClass.forRole(role).forEach { wowClass ->
                                    option(
                                        label = wowClass.displayName,
                                        value = wowClass.name
                                    )
                                }
                            }
                        }
                    )
                }
                return@on
            }

            val updated = eventSignupService
                .signup(eventId, userId, role, existingClass)
                .getOrElse {
                    interaction.respondEphemeral { content = "An error occurred: ${it.message}" }
                    return@on
                }

            val messageId = updated.messageId
                ?: return@on  // or log error

            val channel = interaction.channel

            channel.getMessage(messageId).edit {
                eventMessage(updated)
            }

            interaction.respondEphemeral {
                content = "Your response has been recorded 👍"
            }
        }

        kord.on<SelectMenuInteractionCreateEvent> {
            val id = interaction.componentId
            if (!id.startsWith("class:")) return@on

            val parts = id.split(":")
            val role = Role.valueOf(parts[1])
            val eventId = UUID.fromString(parts[2])

            val userId = interaction.user.id.value.toLong()
            val wowClass = WowClass.valueOf(interaction.values.first())

            logger.info("Saving class for user $userId: $wowClass")

            raidProfileService.saveClass(userId, role, wowClass)

            val updated = eventSignupService
                .signup(eventId, userId, role, wowClass)
                .getOrElse {
                    interaction.respondEphemeral { content = "An error occurred: ${it.message}" }
                    return@on
                }

            val messageId = updated.messageId
                ?: return@on  // or log error

            val channel = interaction.channel

            channel.getMessage(messageId).edit {
                eventMessage(updated)
            }

            interaction.respondEphemeral {
                content = "Signed up as **${wowClass.displayName} ${role.displayName}**!"
            }
        }
    }
}
