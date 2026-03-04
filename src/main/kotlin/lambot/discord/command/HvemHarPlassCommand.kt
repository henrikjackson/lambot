package lambot.discord.command

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.BaseInputChatBuilder
import dev.kord.rest.builder.interaction.string
import lambot.raidhelper.RaidHelperClient
import lambot.raidhelper.RosterService
import lambot.raidhelper.SignUp
import org.springframework.stereotype.Component
import java.time.DayOfWeek

@Component
class HvemHarPlassCommand(
    private val raidHelperClient: RaidHelperClient,
    private val rosterService: RosterService
) : SlashCommand {
    override val name = "hvem-har-plass"
    override val description = "Se hvem som har plass i neste raid"

    override fun buildOptions(builder: BaseInputChatBuilder) {
        builder.string("dag", "Hvilken dag?") {
            required = true
            choice("Onsdag", "wednesday")
            choice("Søndag", "sunday")
        }
    }

    override suspend fun register(kord: Kord) {}

    override suspend fun handle(event: GuildChatInputCommandInteractionCreateEvent) {
        val userId = event.interaction.user.id.toString()
        val day = when (event.interaction.command.strings["dag"]) {
            "sunday" -> DayOfWeek.SUNDAY
            else -> DayOfWeek.WEDNESDAY
        }

        val nextEvent = raidHelperClient.getNextEvent(day)
        if (nextEvent == null) {
            event.interaction.respondEphemeral { content = "Ingen kommende raid funnet." }
            return
        }

        val (roster, bench) = rosterService.buildRoster(nextEvent.signUps.orEmpty())

        val sb = StringBuilder()
        sb.appendLine("**${nextEvent.title}**")
        sb.appendLine()
        sb.appendLine("**I raidet (${roster.size}):**")
        if (roster.isEmpty()) sb.appendLine("_Ingen påmeldt enda._")
        else {
            sb.appendLine("_Nr · Navn (Rolle) · Påmeldt som_")
            roster.forEachIndexed { i, s -> sb.appendLine(formatEntry(i + 1, s, userId)) }
        }

        if (bench.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("**Benk (${bench.size}):**")
            sb.appendLine("_Nr · Navn - (Klasse) · Påmeldt som_")
            bench.forEachIndexed { i, s -> sb.appendLine(formatEntry(i + 1, s, userId)) }
        }

        event.interaction.respondEphemeral { content = sb.toString().trimEnd() }
    }

    private fun formatEntry(pos: Int, signup: SignUp, callerId: String): String {
        val signPos = signup.position?.let { " : $it" } ?: ""
        return if (signup.userId == callerId)
            "$pos. **${signup.name}** - (${signup.className})$signPos ← deg"
        else
            "$pos. ${signup.name} - (${signup.className})$signPos"
    }
}
