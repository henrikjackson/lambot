package lambot.events

import io.ktor.http.renderSetCookieHeader
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

@Service
class EventSignupService(
    private val repository: EventRepository
) {
    fun createEvent(name: String, description: String): Event {
        val event = Event(
            id = UUID.randomUUID(),
            name = name,
            description = description,
            createdAt = LocalDateTime.now()
        )
        
        return repository.save(event)
    }

    fun attend(eventId: UUID, userId: Long): Result<Event> {
        val event = repository.findById(eventId) ?: return Result.failure(IllegalStateException("Event not found"))

        event.notAttending.remove(userId)
        event.attending.add(userId)

        repository.save(event)
        return Result.success(event)
    }

    fun decline(eventId: UUID, userId: Long): Result<Event> {
        val event = repository.findById(eventId) ?: return Result.failure(IllegalStateException("Event not found"))

        event.attending.remove(userId)
        event.notAttending.add(userId)

        repository.save(event)
        return Result.success(event)
    }


    fun listEvents() = repository.findAll()
}