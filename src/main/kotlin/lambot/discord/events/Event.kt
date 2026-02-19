package lambot.discord.events

import java.time.LocalDateTime
import java.util.UUID

data class Event(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String,
    val createdAt: LocalDateTime?,
    val tanks: MutableSet<Long> = mutableSetOf(),
    val healers: MutableSet<Long> = mutableSetOf(),
    val dps: MutableSet<Long> = mutableSetOf(),
    val notAttending: MutableSet<Long> = mutableSetOf()
)
