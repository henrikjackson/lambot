package lambot.discord.role

import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.entity.channel.TextChannel
import dev.kord.rest.builder.message.actionRow
import kotlinx.coroutines.flow.firstOrNull
import lambot.config.DiscordProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class RoleMessageService(
    private val properties: DiscordProperties
) {
    private val logger = LoggerFactory.getLogger(RoleMessageService::class.java)

    val marker = "**Hva vil du raide i Midnight?**"

    @Volatile
    var roleMessageId: Snowflake? = null

    suspend fun initialize(kord: Kord) {
        roleMessageId = findOrCreate(kord)
        logger.info("Role message ID set to: $roleMessageId")
    }

    suspend fun refresh(kord: Kord) {
        val channelId = Snowflake(properties.roleMessageChannelId)
        val channel = kord.getChannelOf<TextChannel>(channelId) ?: return

        roleMessageId?.let { channel.getMessageOrNull(it)?.delete() }
        roleMessageId = null

        roleMessageId = post(kord, channel)
        logger.info("Refreshed role message, new ID: $roleMessageId")
    }

    private suspend fun findOrCreate(kord: Kord): Snowflake? {
        val channelId = Snowflake(properties.roleMessageChannelId)
        val channel = kord.getChannelOf<TextChannel>(channelId) ?: run {
            logger.error("Role message channel not found: ${properties.roleMessageChannelId}")
            return null
        }

        val existing = channel.getMessagesBefore(Snowflake.max, 50)
            .firstOrNull {
                it.author?.id == kord.selfId &&
                it.content.startsWith(marker) &&
                it.actionRows.isNotEmpty()
            }

        if (existing != null) {
            logger.info("Reusing existing role message: ${existing.id}")
            return existing.id
        }

        return post(kord, channel)
    }

    private suspend fun post(kord: Kord, channel: TextChannel): Snowflake? {
        val content = buildString {
            appendLine(marker)
            appendLine()
            appendLine("Trykk på en knapp for å velge ønsket vanskelighetsgrad:")
            appendLine()
            appendLine("Mythic-rollen kommer til å tagges før hver Mythic-raid for å finne ut hvem som kan delta.")
            appendLine()
            appendLine("Du kan endre preferanse når du vil!")
            appendLine()
            append("_Velg bare én!_")
        }

        val message = channel.createMessage {
            this.content = content
            actionRow {
                properties.roleAssignments.forEach { assignment ->
                    interactionButton(ButtonStyle.Primary, "role:${assignment.roleId}") {
                        label = "${assignment.emoji} ${assignment.label}"
                    }
                }
                interactionButton(ButtonStyle.Danger, "role:remove") {
                    label = "Fjern rolle"
                }
            }
        }

        logger.info("Posted new role message: ${message.id}")
        return message.id
    }
}
