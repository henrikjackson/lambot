package lambot.discord.events

import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.component.ActionRowBuilder
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.DiscordPartialEmoji

fun MessageBuilder.eventMessage(event: Event) {
    content = buildString {
        appendLine("🎉 **${event.name}**")
        appendLine(event.description)
        appendLine()

        renderRole("🛡 Tank", event.tanks)
        renderRole("💚 Healer", event.healers)
        renderRole("⚔ DPS", event.dps)
        renderRole("❌ Not Attending", event.notAttending)
    }

    components = mutableListOf(
        ActionRowBuilder().apply {
            interactionButton(ButtonStyle.Primary, "event:tank:${event.id}") {
                label = "Tank"
                emoji = DiscordPartialEmoji(name = "🛡")
            }
            interactionButton(ButtonStyle.Success, "event:healer:${event.id}") {
                label = "Healer"
                emoji = DiscordPartialEmoji(name = "💚")
            }
            interactionButton(ButtonStyle.Secondary, "event:dps:${event.id}") {
                label = "DPS"
                emoji = DiscordPartialEmoji(name = "⚔")
            }
            interactionButton(ButtonStyle.Danger, "event:na:${event.id}") {
                label = "Not Attending"
            }
        })
    }


private fun StringBuilder.renderRole(
    title: String,
    users: Set<Long>
) {
    appendLine("**$title (${users.size})**")
    if (users.isEmpty()) {
        appendLine("_No signups_")
    } else {
        users.forEach {
            appendLine("• <@$it>")
        }
    }
    appendLine()
}

