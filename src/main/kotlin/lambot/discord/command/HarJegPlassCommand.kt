package lambot.discord.command

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.BaseInputChatBuilder
import dev.kord.rest.builder.interaction.string
import lambot.raidhelper.RaidHelperClient
import lambot.raidhelper.RosterResult
import lambot.raidhelper.RosterService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.DayOfWeek

@Component
class HarJegPlassCommand(
    private val raidHelperClient: RaidHelperClient,
    private val rosterService: RosterService
) : SlashCommand {
    private val logger = LoggerFactory.getLogger(HarJegPlassCommand::class.java)

    override val name = "har-jeg-plass"
    override val description = "Sjekk om du har plass i raidet"

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
        logger.info("userId from Discord: $userId")
        logger.info("signUps userIds: ${nextEvent?.signUps?.map { it.userId }}")
        if (nextEvent == null) {
            event.interaction.respondEphemeral { content = "Ingen kommende raid funnet." }
            return
        }

        val result = rosterService.evaluate(nextEvent.signUps.orEmpty(), userId)
        val message = when (result) {
            is RosterResult.InRoster -> "Du har plass i raidet! (Plass #${result.position})"
            is RosterResult.Benched -> "Du er på benken. Kø-posisjon: #${result.queuePosition}"
            is RosterResult.NotSignedUp -> "Du er ikke påmeldt neste raid."
        }

        event.interaction.respondEphemeral { content = message }
    }
}
