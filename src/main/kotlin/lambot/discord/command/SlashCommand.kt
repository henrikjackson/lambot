package lambot.discord.command

import dev.kord.core.Kord
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.BaseInputChatBuilder

interface SlashCommand {
    val name: String
    val description: String

    suspend fun register(kord: Kord)

    fun buildOptions(builder: BaseInputChatBuilder) {}

    suspend fun handle(event: GuildChatInputCommandInteractionCreateEvent)
}
