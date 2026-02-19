package lambot.discord.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "bot.schedule")
class BotScheduleProperties {
    var channelId: Long = 0
    var guildId: Long = 0
}