package lambot.discord.events

import dev.kord.common.entity.Snowflake
import lambot.raid.Role
import lambot.raid.WowClass
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class EventSignupService(
    private val repository: EventRepository
) {

    fun createEvent(
        name: String,
        description: String
    ): Event {
        val event = Event(
            id = UUID.randomUUID(),
            name = name,
            description = description,
            createdAt = LocalDateTime.now()
        )

        return repository.save(event)
    }

    @Transactional
    fun signup(
        eventId: UUID,
        userId: Long,
        role: Role,
        wowClass: WowClass?
    ): Result<Event> {

        val event = repository.findById(eventId)
            .orElse(null)
            ?: return Result.failure(IllegalStateException("Event not found"))

        // Remove user from all roles
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

        // No need to call save() explicitly inside a transaction,
        // but it's fine to keep it for clarity
        return Result.success(repository.save(event))
    }

    @Transactional
    fun attachMessageId(
        eventId: UUID,
        messageId: Snowflake
    ) {
        val event = repository.findById(eventId)
            .orElseThrow { IllegalStateException("Event not found") }

        // Store Snowflake as Long
        event.messageId = messageId.value.toLong()

        repository.save(event)
    }
}
