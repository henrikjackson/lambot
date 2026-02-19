package lambot.discord.events

import dev.kord.common.entity.Snowflake
import lambot.raid.Role
import lambot.raid.WowClass
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

    fun signup(eventId: UUID, userId: Long, role: Role, wowClass: WowClass?): Result<Event> {
        val event = repository.findById(eventId) ?: return Result.failure(IllegalStateException("Event not found"))

        event.tanks.remove(userId)
        event.healers.remove(userId)
        event.dps.remove(userId)
        event.notAttending.remove(userId)

        when (role) {
            Role.TANK -> event.tanks[userId] = wowClass!!
            Role.HEALER -> event.healers[userId] = wowClass!!
            Role.DPS -> event.dps[userId] = wowClass!!
            Role.NOT_ATTENDING -> event.notAttending.add(userId)
        }

        repository.save(event)
        return Result.success(event)
    }

    fun attachMessageId(eventId: UUID, messageId: Snowflake) {
        val event = repository.findById(eventId) ?: return

        event.messageId = messageId
        repository.save(event)
    }
}