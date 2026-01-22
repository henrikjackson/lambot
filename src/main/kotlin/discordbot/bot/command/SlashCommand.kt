package discordbot.bot.command

import dev.kord.core.Kord
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent

interface SlashCommand {
    val name: String

    fun register(kord: Kord)

    suspend fun handle(event: GuildChatInputCommandInteractionCreateEvent)
}