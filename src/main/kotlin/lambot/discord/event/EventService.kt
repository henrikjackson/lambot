package lambot.discord.event

import dev.kord.common.entity.Snowflake
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EventService {
    private val events = mutableMapOf<Snowflake, EventState>()

    private val logger = LoggerFactory.getLogger(EventService::class.java)

    fun create(messageId: Snowflake): EventState {
        val event = EventState(messageId)
        events[messageId] = event
        return event
    }

    fun get(messageId: Snowflake): EventState? = events[messageId]

    fun respondYes(messageId: Snowflake, userId: Snowflake) {
        logger.info("Responding to event: $messageId")
        val event = events[messageId] ?: return
        event.no.remove(userId)
        event.yes.add(userId)
    }

    fun respondNo(messageId: Snowflake, userId: Snowflake) {
        val event = events[messageId] ?: return
        event.yes.remove(userId)
        event.no.add(userId)
    }
}