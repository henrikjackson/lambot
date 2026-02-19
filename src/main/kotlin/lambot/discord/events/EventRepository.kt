package lambot.discord.events

import org.springframework.stereotype.Repository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Repository
class EventRepository {
    private val events = ConcurrentHashMap<UUID, Event>()

    fun save(event: Event): Event {
        events[event.id] = event
        return event
    }

    fun findById(id: UUID) = events[id]

    fun findAll() = events.values.toList()

    fun deleteById(id: UUID) = events.remove(id)
}