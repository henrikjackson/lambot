package lambot.raidhelper

import lambot.config.RaidHelperProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId

@Component
class RaidHelperClient(private val properties: RaidHelperProperties) {

    private val logger = LoggerFactory.getLogger(RaidHelperClient::class.java)

    private val client = RestClient.builder()
        .baseUrl(properties.baseUrl)
        .defaultHeader("Authorization", properties.apiKey)
        .build()

    fun getNextEvent(day: DayOfWeek): RaidEvent? {
        val channelId = if (day == DayOfWeek.WEDNESDAY) properties.wednesdayChannelId else properties.sundayChannelId
        val now = Instant.now().epochSecond
        val response = client.get()
            .uri("/v3/servers/${properties.serverId}/events?StartTimeFilter=$now&Page=1")
            .retrieve()
            .body(EventsResponse::class.java)
        val eventId = response?.postedEvents?.firstOrNull { it.channelId == channelId }?.id ?: return null
        return client.get()
            .uri("/v2/events/$eventId")
            .retrieve()
            .body(RaidEvent::class.java)
    }

    fun createEvent(request: CreateEventRequest, channelId: String) {
        client.post()
            .uri("/v2/servers/${properties.serverId}/channels/$channelId/event")
            .header("Content-Type", "application/json")
            .body(request)
            .retrieve()
            .toBodilessEntity()
        logger.info("Created event: ${request.title}")
    }
}
