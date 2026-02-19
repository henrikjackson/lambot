package lambot.discord.events

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import lambot.raid.WowClass
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "events")
class Event(

    @Id
    val id: UUID,

    val name: String,

    val description: String,

    val createdAt: LocalDateTime,

    @Column(nullable = true)
    var messageId: Long? = null,

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var tanks: MutableMap<Long, WowClass> = mutableMapOf(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var healers: MutableMap<Long, WowClass> = mutableMapOf(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var dps: MutableMap<Long, WowClass> = mutableMapOf(),

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    var notAttending: MutableSet<Long> = mutableSetOf()
)
