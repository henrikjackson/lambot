package lambot.discord.command

import dev.kord.core.Kord
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent

interface SlashCommand {
    val name: String

    suspend fun register(kord: Kord)

    suspend fun handle(event: GuildChatInputCommandInteractionCreateEvent)
}