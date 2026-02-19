package lambot.discord.command

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import org.springframework.stereotype.Component
import dev.kord.rest.builder.interaction.string
import lambot.discord.config.BotScheduleProperties
import lambot.discord.events.EventSignupService
import org.slf4j.LoggerFactory
import dev.kord.core.behavior.channel.createMessage
import lambot.discord.events.eventMessage


@Component
class CreateEventCommand(
    private val eventSignupService: EventSignupService,
    private val botScheduleProperties: BotScheduleProperties
) : SlashCommand {
    override val name = "event"

    private val logger = LoggerFactory.getLogger(CreateEventCommand::class.java)

    override suspend fun register(kord: Kord) {
        kord.createGuildChatInputCommand(
            guildId = Snowflake(botScheduleProperties.guildId),
            name = name,
            description = "Create a new event"
        ) {
            string("name", "Event name") {
                required = true
            }
            string("description", "Event description") {
                required = true
            }
        }
    }

    override suspend fun handle(
        event: GuildChatInputCommandInteractionCreateEvent
    ) {
        logger.info("Received event creation command!")
        val interaction = event.interaction

        val name = interaction.command.strings["name"] ?: return
        val description = interaction.command.strings["description"] ?: return

        val createdEvent = eventSignupService.createEvent(
            name = name,
            description = description
        )

        val kord = interaction.kord
        val channelId = Snowflake(botScheduleProperties.channelId)

        val channel = kord.getChannelOf<MessageChannel>(channelId)
            ?: error("Channel not found")

        val message = channel.createMessage {
            eventMessage(createdEvent)
        }

        eventSignupService.attachMessageId(createdEvent.id, message.id)

        interaction.respondPublic {
            content = "✅ Event **${createdEvent.name}** created!"
        }
    }
}