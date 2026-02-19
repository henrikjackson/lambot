package lambot.discord.events

import dev.kord.common.entity.Snowflake
import lambot.raid.WowClass
import java.time.LocalDateTime
import java.util.UUID

data class Event(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val description: String,
    var messageId: Snowflake? = null,
    val createdAt: LocalDateTime?,
    val tanks: MutableMap<Long, WowClass> = mutableMapOf(),
    val healers: MutableMap<Long, WowClass> = mutableMapOf(),
    val dps: MutableMap<Long, WowClass> = mutableMapOf(),
    val notAttending: MutableSet<Long> = mutableSetOf()
)
