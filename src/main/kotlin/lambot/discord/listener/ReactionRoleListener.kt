package lambot.discord.listener

import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.entity.ReactionEmoji
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.message.ReactionAddEvent
import dev.kord.core.event.message.ReactionRemoveEvent
import dev.kord.core.on
import lambot.config.DiscordProperties
import lambot.config.FeaturesProperties
import lambot.discord.role.RoleMessageService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ReactionRoleListener(
    private val properties: DiscordProperties,
    private val features: FeaturesProperties,
    private val roleMessageService: RoleMessageService
) : DiscordListener {

    private val logger = LoggerFactory.getLogger(ReactionRoleListener::class.java)

    override fun register(kord: Kord) {
        if (!features.reactionRoles) {
            logger.info("Reaction roles feature is disabled")
            return
        }
        if (properties.roleMessageChannelId.isBlank() || properties.roleAssignments.isEmpty()) {
            logger.info("Role assignments not configured, skipping ReactionRoleListener")
            return
        }

        kord.on<ReadyEvent> {
            roleMessageService.initialize(kord)
        }

        kord.on<ReactionAddEvent> {
            val msgId = roleMessageService.roleMessageId ?: return@on
            if (messageId != msgId || userId == kord.selfId) return@on

            val emojiName = (emoji as? ReactionEmoji.Unicode)?.name ?: return@on
            val assignment = properties.roleAssignments.find { it.emoji == emojiName } ?: return@on
            val guildId = this.guildId ?: return@on

            try {
                val guild = kord.getGuildOrNull(guildId) ?: return@on
                val member = guild.getMemberOrNull(userId) ?: return@on
                val message = kord.getChannelOf<MessageChannel>(channelId)?.getMessageOrNull(messageId)

                properties.roleAssignments
                    .filter { it.roleId != assignment.roleId }
                    .forEach { other ->
                        message?.deleteReaction(userId, ReactionEmoji.Unicode(other.emoji))
                        if (Snowflake(other.roleId) in member.roleIds) {
                            val otherRoleName = guild.getRoleOrNull(Snowflake(other.roleId))?.name ?: other.roleId
                            member.removeRole(Snowflake(other.roleId))
                            logger.info("Removed conflicting role $otherRoleName from user ${member.effectiveName} ($userId)")
                        }
                    }

                val roleName = guild.getRoleOrNull(Snowflake(assignment.roleId))?.name ?: assignment.roleId
                member.addRole(Snowflake(assignment.roleId))
                logger.info("Added role $roleName to user ${member.effectiveName} ($userId)")
            } catch (e: Exception) {
                logger.error("Failed to add role ${assignment.roleId} to user $userId: ${e.message}", e)
            }
        }

        kord.on<ReactionRemoveEvent> {
            val msgId = roleMessageService.roleMessageId ?: return@on
            if (messageId != msgId) return@on

            val emojiName = (emoji as? ReactionEmoji.Unicode)?.name ?: return@on
            val assignment = properties.roleAssignments.find { it.emoji == emojiName } ?: return@on
            val guildId = this.guildId ?: return@on

            try {
                val guild = kord.getGuildOrNull(guildId) ?: return@on
                val member = guild.getMemberOrNull(userId) ?: return@on
                val roleName = guild.getRoleOrNull(Snowflake(assignment.roleId))?.name ?: assignment.roleId
                member.removeRole(Snowflake(assignment.roleId))
                logger.info("Removed role $roleName from user ${member.effectiveName} ($userId)")
            } catch (e: Exception) {
                logger.error("Failed to remove role ${assignment.roleId} from user $userId: ${e.message}", e)
            }
        }
    }
}
