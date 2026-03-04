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
        val now = Instant.now().epochSecond
        val response = client.get()
            .uri("/v3/servers/${properties.serverId}/events?StartTimeFilter=$now&Page=1")
            .header("IncludeSignUps", "true")
            .retrieve()
            .body(EventsResponse::class.java)
        return response?.postedEvents?.firstOrNull { event ->
            Instant.ofEpochSecond(event.startTime)
                .atZone(ZoneId.of("Europe/Oslo"))
                .dayOfWeek == day
        }
    }

    fun createEvent(request: CreateEventRequest) {
        client.post()
            .uri("/v2/servers/${properties.serverId}/channels/${properties.channelId}/event")
            .header("Content-Type", "application/json")
            .body(request)
            .retrieve()
            .toBodilessEntity()
        logger.info("Created event: ${request.title}")
    }
}
