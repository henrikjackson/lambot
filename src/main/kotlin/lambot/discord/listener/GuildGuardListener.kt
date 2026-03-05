package lambot.discord.listener

import dev.kord.core.Kord
import dev.kord.core.event.guild.GuildCreateEvent
import dev.kord.core.on
import lambot.config.DiscordProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class GuildGuardListener(
    private val properties: DiscordProperties
) : DiscordListener {

    private val logger = LoggerFactory.getLogger(GuildGuardListener::class.java)

    override fun register(kord: Kord) {
        kord.on<GuildCreateEvent> {
            val allowedIds = (properties.allowedGuildIds + properties.guildId).toSet()
            if (guild.id.toString() !in allowedIds) {
                logger.warn("Joined unauthorized guild '${guild.name}' (${guild.id}), leaving immediately")
                guild.leave()
            }
        }
    }
}
