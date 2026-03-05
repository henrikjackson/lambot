package lambot.discord.role

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.channel.TextChannel
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
            .firstOrNull { it.author?.id == kord.selfId && it.content.startsWith(marker) }

        if (existing != null) {
            logger.info("Reusing existing role message: ${existing.id}")
            return existing.id
        }

        return post(kord, channel)
    }

    private suspend fun post(kord: Kord, channel: TextChannel): Snowflake? {
        val guild = kord.getGuildOrNull(Snowflake(properties.guildId)) ?: return null

        val roleLines = properties.roleAssignments.mapNotNull { assignment ->
            val roleName = guild.getRoleOrNull(Snowflake(assignment.roleId))?.name ?: return@mapNotNull null
            "${assignment.emoji} – $roleName"
        }

        val content = buildString {
            appendLine(marker)
            appendLine("Reager med emoji for å få en rolle:")
            appendLine()
            roleLines.forEach { appendLine("**$it**") }
            appendLine()
            appendLine("Mythic-rollen kommer til å tagges før hver Mythic-raid for å finne ut hvem som kan delta.")
            appendLine()
            appendLine("Du kan endre prefesanse når du vil!")
            appendLine()
            append("_Velg bare én! Fjern reaksjonen din for å fjerne rollen. Ikke spam ned med reaksjoner! Dobbeltsjekk egen bruker om du har fått riktig rolle_")
        }

        val message = channel.createMessage(content)
        properties.roleAssignments.forEach { assignment ->
            message.addReaction(ReactionEmoji.Unicode(assignment.emoji))
        }
        logger.info("Posted new role message: ${message.id}")
        return message.id
    }
}
