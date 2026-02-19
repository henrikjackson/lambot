package lambot.events

import java.util.UUID

data class Event(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String,
    val createdAt: java.time.LocalDateTime?,
    val attending: MutableSet<Long> = mutableSetOf(),
    val notAttending: MutableSet<Long> = mutableSetOf()
)
