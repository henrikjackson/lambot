package lambot.discord.event

import dev.kord.common.entity.Snowflake

data class EventState(
    val messageId: Snowflake,
    val yes: MutableSet<Snowflake> = mutableSetOf(),
    val no: MutableSet<Snowflake> = mutableSetOf()
)
