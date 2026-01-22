package discordbot.bot.scheduler

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.channel.MessageChannel
import discordbot.bot.BotStartup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RecurringMessageScheduler(
    private val bot: BotStartup
) {
    private val logger = LoggerFactory.getLogger(RecurringMessageScheduler::class.java)
    private val scope = CoroutineScope(Dispatchers.Default)

    private val channelId = Snowflake("784044052248133633") // TODO Dra ut


    @Scheduled(cron = "\${bot.schedule.cron}", zone = "Europe/Oslo")
    fun sendRecurringMessage() {
        logger.info("Sending recurring message...")

        scope.launch {
            val kord = bot.kordOrNull() ?: return@launch
            val channel = kord.getChannelOf<MessageChannel>(channelId) ?: return@launch

            channel.createMessage("Hello!")
        }
    }
}