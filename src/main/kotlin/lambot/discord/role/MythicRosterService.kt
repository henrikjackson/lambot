package lambot.discord.role

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.entity.GuildEmoji
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.toList
import lambot.config.DiscordProperties
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MythicRosterService(
    private val properties: DiscordProperties,
    private val userDataService: UserDataService
) {
    private val logger = LoggerFactory.getLogger(MythicRosterService::class.java)

    val marker = "**Mythic-roster**"

    @Volatile
    private var rosterMessageId: Snowflake? = null

    suspend fun initialize(kord: Kord) {
        if (properties.mythicRosterChannelId.isBlank()) return
        rosterMessageId = findOrCreate(kord)
        update(kord)
    }

    suspend fun update(kord: Kord) {
        if (properties.mythicRosterChannelId.isBlank()) return
        val channelId = Snowflake(properties.mythicRosterChannelId)
        val channel = kord.getChannelOf<TextChannel>(channelId) ?: return
        val content = buildContent(kord)

        val msgId = rosterMessageId
        if (msgId != null && channel.getMessageOrNull(msgId) != null) {
            channel.getMessageOrNull(msgId)!!.edit { this.content = content }
        } else {
            val message = channel.createMessage { this.content = content }
            rosterMessageId = message.id
            logger.info("Created new mythic roster message: ${message.id}")
        }
    }

    private suspend fun buildContent(kord: Kord): String {
        val guild = kord.getGuildOrNull(Snowflake(properties.guildId))
            ?: return "$marker\n\n_Feil: kunne ikke hente serveren._"

        val userData = userDataService.getAll()
        if (userData.isEmpty()) {
            return "$marker\n\n_Ingen Mythic-raiders registrert enda._"
        }

        val emojiMap = guild.emojis.toList().associateBy { it.name }

        data class Entry(val userId: String, val name: String, val wowSpec: String, val wowClass: String, val role: WowRole)

        val entries = userData.values
            .mapNotNull { data ->
                val member = guild.getMemberOrNull(Snowflake(data.userId)) ?: return@mapNotNull null
                val role = WowClasses.roleFor(data.wowClass, data.wowSpec) ?: return@mapNotNull null
                Entry(data.userId, member.effectiveName, data.wowSpec, data.wowClass, role)
            }
            .sortedWith(compareBy({ it.role.ordinal }, { it.name.lowercase() }))

        return buildString {
            appendLine(marker)
            appendLine()
            if (entries.isEmpty()) {
                append("_Ingen Mythic-raiders registrert enda._")
            } else {
                appendLine("**${entries.size} raiders:**")
                entries.groupBy { it.role }.entries
                    .sortedBy { it.key.ordinal }
                    .forEach { (role, group) ->
                        appendLine()
                        appendLine("**${role.label} (${group.size}):**")
                        group.forEach {
                            val emoji = emojiString(emojiMap, it.wowClass, it.wowSpec)
                            appendLine("$emoji<@${it.userId}> — ${it.wowSpec} ${it.wowClass}")
                        }
                    }
            }
        }.trimEnd()
    }

    private fun emojiString(emojiMap: Map<String?, GuildEmoji>, className: String, specName: String): String {
        val name = WowClasses.specEmojiName(className, specName) ?: return ""
        val emoji = emojiMap[name] ?: return ""
        return "<:${emoji.name}:${emoji.id}> "
    }

    private suspend fun findOrCreate(kord: Kord): Snowflake? {
        val channelId = Snowflake(properties.mythicRosterChannelId)
        val channel = kord.getChannelOf<TextChannel>(channelId) ?: run {
            logger.error("Mythic roster channel not found: ${properties.mythicRosterChannelId}")
            return null
        }

        val existing = channel.getMessagesBefore(Snowflake.max, 10)
            .firstOrNull { it.author?.id == kord.selfId && it.content.startsWith(marker) }

        if (existing != null) {
            logger.info("Reusing existing mythic roster message: ${existing.id}")
            return existing.id
        }

        val message = channel.createMessage { content = marker }
        logger.info("Created mythic roster message placeholder: ${message.id}")
        return message.id
    }
}
