package lambot.discord.command

import dev.kord.core.Kord
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.RootInputChatBuilder

interface SlashCommand {
    val name: String
    val description: String
    val requiredRoleIds: Set<String> get() = emptySet()

    suspend fun register(kord: Kord)

    fun buildOptions(builder: RootInputChatBuilder) {}

    suspend fun handle(event: GuildChatInputCommandInteractionCreateEvent)
}
