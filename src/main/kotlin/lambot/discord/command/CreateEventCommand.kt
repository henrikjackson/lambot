package lambot.discord.command

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import org.springframework.stereotype.Component
import dev.kord.rest.builder.interaction.string
import lambot.discord.config.BotScheduleProperties
import lambot.events.EventSignupService
import dev.kord.core.behavior.interaction.updatePublicMessage
import org.slf4j.LoggerFactory


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

        logger.info("Interaction appId = ${interaction.applicationId}")
        val name = interaction.command.strings["name"]
            ?: return

        logger.info("Creating event: $name")
        val description = interaction.command.strings["description"]
            ?: return

        logger.info("Creating message...")
        val createdEvent = eventSignupService.createEvent(
            name = name,
            description = description
        )

        logger.info("Created event: ${createdEvent.id}")
        interaction.respondPublic {
            content = """
                🎉 **Event created!**
                
                **${createdEvent.name}**
                ${createdEvent.description}
                
                Event ID: `${createdEvent.id}`
            """.trimIndent()
        }
    }

    private fun buildMessage(
        yes: List<String>,
        no: List<String>
    ): String =
        """
        📅 **New Event**

        ✅ **Yes (${yes.size})**
        ${yes.joinToString("\n") { "• $it" }.ifBlank { "_No responses yet_" }}

        ❌ **No (${no.size})**
        ${no.joinToString("\n") { "• $it" }.ifBlank { "_No responses yet_" }}
        """.trimIndent()
}