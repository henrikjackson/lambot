package lambot.discord.scheduler

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.MessageChannel
import lambot.discord.BotStartup
import kotlinx.coroutines.launch
import lambot.discord.config.BotScheduleProperties
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RecurringMessageScheduler(
    private val bot: BotStartup,
    private val scheduleProperties: BotScheduleProperties
) {
    private val logger = LoggerFactory.getLogger(RecurringMessageScheduler::class.java)

    private val channelId = Snowflake(scheduleProperties.channelId)

    private val channel: MessageChannel? = null


    @Scheduled(cron = "\${bot.schedule.cron}", zone = "Europe/Oslo")
    fun sendRecurringMessage() {
        logger.info("Sending recurring message...")

        bot.scope().launch {
            val kord = bot.kordOrNull() ?: run {
                logger.error("Kord instance not available!")
                return@launch
            }

            val channel = channel ?: kord.getChannelOf<MessageChannel>(channelId) ?: return@launch

            channel.createMessage("Hello!")
        }
    }
}