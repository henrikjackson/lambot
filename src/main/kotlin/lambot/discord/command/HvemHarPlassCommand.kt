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
        sb.append(formatTable(roster.mapIndexed { i, s -> i + 1 to s }, userId))

        if (bench.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("**Benk (${bench.size}):**")
            sb.append(formatTable(bench.mapIndexed { i, s -> i + 1 to s }, userId))
        }

        event.interaction.respondEphemeral { content = sb.toString().trimEnd() }
    }

    private fun formatTable(entries: List<Pair<Int, SignUp>>, callerId: String): String {
        val nameWidth = 20
        val classWidth = 10
        val header = " #   ${"Navn".padEnd(nameWidth)} ${"Klasse".padEnd(classWidth)}  Sign"
        val separator = "-".repeat(header.length)
        val rows = entries.map { (pos, signup) ->
            val name = signup.name.take(nameWidth).padEnd(nameWidth)
            val cls = signup.className.take(classWidth).padEnd(classWidth)
            val signPos = signup.position?.let { "#$it" } ?: "-"
            val marker = if (signup.userId == callerId) " ←" else ""
            " %-3d %s %s  %-4s%s".format(pos, name, cls, signPos, marker)
        }
        return "```\n$header\n$separator\n${rows.joinToString("\n")}\n```\n"
    }
}
