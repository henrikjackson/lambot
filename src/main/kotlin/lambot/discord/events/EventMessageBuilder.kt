package lambot.discord.events

import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.common.entity.ButtonStyle

fun MessageBuilder.eventMessage(event: Event) {
    content = buildString {
        appendLine("🎉 **${event.name}**")
        appendLine(event.description)
        appendLine()

        appendLine("✅ **Yes (${event.attending.size})**")
        if (event.attending.isEmpty()) {
            appendLine("_No responses yet_")
        } else {
            event.attending.forEach {
                appendLine("• <@$it>")
            }
        }

        appendLine()
        appendLine("❌ **No (${event.notAttending.size})**")
        if (event.notAttending.isEmpty()) {
            appendLine("_No responses yet_")
        } else {
            event.notAttending.forEach {
                appendLine("• <@$it>")
            }
        }
    }

    components = mutableListOf(
        ActionRowBuilder().apply {
            interactionButton(
                style = ButtonStyle.Success,
                customId = "event:yes:${event.id}"
            ) {
                label = "Yes"
            }

            interactionButton(
                style = ButtonStyle.Danger,
                customId = "event:no:${event.id}"
            ) {
                label = "No"
            }
        }
    )
}

