package lambot.discord.scheduler

import lambot.config.FeaturesProperties
import lambot.config.RaidHelperProperties
import lambot.raidhelper.AdvancedSettings
import lambot.raidhelper.CreateEventRequest
import lambot.raidhelper.RaidHelperClient
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

@Component
class EventScheduler(
    private val raidHelperClient: RaidHelperClient,
    private val properties: RaidHelperProperties,
    private val features: FeaturesProperties
) {
    private val logger = LoggerFactory.getLogger(EventScheduler::class.java)

    @Scheduled(cron = "0 0 22 * * WED", zone = "Europe/Oslo")
    fun scheduleNextWednesdayEvent() {
        scheduleRaidOn(DayOfWeek.WEDNESDAY, properties.wednesdayChannelId, "https://i.imgur.com/BPmucLG.jpeg", "Onsdagsraid")
    }

    @Scheduled(cron = "0 0 22 * * SUN", zone = "Europe/Oslo")
    fun scheduleNextSundayEvent() {
        scheduleRaidOn(DayOfWeek.SUNDAY, properties.sundayChannelId, "https://i.imgur.com/b89yUFX.jpeg", "Sondagsraid")
    }

    private fun scheduleRaidOn(day: DayOfWeek, channelId: String, imageUrl: String, title: String) {
        if (!features.autoCreateEvents) {
            logger.info("Auto-create events is disabled, skipping.")
            return
        }
        val nextDay = LocalDate.now().with(TemporalAdjusters.next(day))
        val raidTime = LocalDateTime.of(nextDay.year, nextDay.month, nextDay.dayOfMonth, 19, 0)
        val unixTimestamp = raidTime.atZone(ZoneId.of("Europe/Oslo")).toEpochSecond().toString()

        try {
            raidHelperClient.createEvent(
                CreateEventRequest(
                    leaderId = properties.leaderId,
                    templateId = properties.templateId,
                    date = unixTimestamp,
                    time = unixTimestamp,
                    title = title,
                    description = "",
                    advancedSettings = AdvancedSettings(image = imageUrl)
                ),
                channelId
            )
            logger.info("Scheduled next raid event: $title")
        } catch (e: Exception) {
            logger.error("Failed to create next raid event: ${e.message}", e)
        }
    }
}
