package lambot.discord.command

import dev.kord.common.entity.ButtonStyle
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.core.on
import org.springframework.stereotype.Component
import dev.kord.core.behavior.channel.createMessage
import dev.kord.rest.builder.component.ActionRowBuilder



@Component
class CreateEventCommand : SlashCommand {
    override val name = "event"

    override fun register(kord: dev.kord.core.Kord) {
        kord.on<GuildChatInputCommandInteractionCreateEvent> {
            if (interaction.command.rootName != name) return@on
            handle(this)
        }
    }

    override suspend fun handle(event: GuildChatInputCommandInteractionCreateEvent) {
        val channel = event.interaction.channel

        channel.createMessage {
            content = buildMessage(
                yes = emptyList(),
                no = emptyList()
            )

            components = mutableListOf(
                ActionRowBuilder().apply {
                    interactionButton(
                        style = ButtonStyle.Success,
                        customId = "event:yes",
                    ) { label = "Yes" }

                    interactionButton(
                        style = ButtonStyle.Danger,
                        customId = "event:no",
                    ) { label = "No" }
                }
            )
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