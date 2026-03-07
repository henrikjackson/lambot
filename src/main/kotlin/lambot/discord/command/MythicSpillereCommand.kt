package lambot.discord.command

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.response.respond
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.RootInputChatBuilder
import lambot.discord.role.UserDataService
import lambot.discord.role.WowClasses
import lambot.discord.role.WowRole
import org.springframework.stereotype.Component

@Component
class MythicSpillereCommand(
    private val userDataService: UserDataService
) : SlashCommand {

    override val name = "mythic-spillere"
    override val description = "Se alle Mythic-raiders og deres klasse/spec"

    override fun buildOptions(builder: RootInputChatBuilder) {}

    override suspend fun register(kord: Kord) {}

    override suspend fun handle(event: GuildChatInputCommandInteractionCreateEvent) {
        val response = event.interaction.deferEphemeralResponse()

        val userData = userDataService.getAll()
        if (userData.isEmpty()) {
            response.respond { content = "Ingen Mythic-raiders registrert enda." }
            return
        }

        val guild = event.interaction.getGuild()

        data class Entry(val name: String, val wowSpec: String, val wowClass: String, val role: WowRole)

        val entries = userData.values
            .mapNotNull { data ->
                val member = guild.getMemberOrNull(Snowflake(data.userId)) ?: return@mapNotNull null
                val role = WowClasses.roleFor(data.wowClass, data.wowSpec) ?: return@mapNotNull null
                Entry(member.effectiveName, data.wowSpec, data.wowClass, role)
            }
            .sortedWith(compareBy({ it.role.ordinal }, { it.name.lowercase() }))

        if (entries.isEmpty()) {
            response.respond { content = "Ingen Mythic-raiders registrert enda." }
            return
        }

        val sb = StringBuilder()
        sb.appendLine("**Mythic-raiders (${entries.size}):**")
        entries.groupBy { it.role }.entries
            .sortedBy { it.key.ordinal }
            .forEach { (role, group) ->
                sb.appendLine()
                sb.appendLine("**${role.label} (${group.size}):**")
                group.forEach { sb.appendLine("${it.name} — ${it.wowSpec} ${it.wowClass}") }
            }

        response.respond { content = sb.toString().trimEnd() }
    }
}
