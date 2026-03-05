package lambot.discord.command

import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.response.edit
import dev.kord.core.entity.interaction.SubCommand
import dev.kord.core.event.interaction.GuildChatInputCommandInteractionCreateEvent
import dev.kord.rest.builder.interaction.RootInputChatBuilder
import dev.kord.rest.builder.interaction.subCommand
import lambot.config.DiscordProperties
import lambot.discord.role.RoleMessageService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AdminCommand(
    private val discordProperties: DiscordProperties,
    private val roleMessageService: RoleMessageService
) : SlashCommand {

    private val logger = LoggerFactory.getLogger(AdminCommand::class.java)

    override val name = "admin"
    override val description = "Admin commands"
    override val requiredRoleIds: Set<String> get() = discordProperties.allowedRoleIds.toSet()

    @Volatile
    private var kord: Kord? = null

    override suspend fun register(kord: Kord) {
        this.kord = kord
    }

    override fun buildOptions(builder: RootInputChatBuilder) {
        builder.subCommand("refresh-rolleoppdrag", "Slett og gjenopprett rolleoppdragsmelding") {}
    }

    override suspend fun handle(event: GuildChatInputCommandInteractionCreateEvent) {
        val subCommand = event.interaction.command as? SubCommand ?: return
        when (subCommand.name) {
            "refresh-rolleoppdrag" -> handleRefresh(event)
        }
    }

    private suspend fun handleRefresh(event: GuildChatInputCommandInteractionCreateEvent) {
        val kord = this.kord ?: return
        val response = event.interaction.respondEphemeral { content = "Oppdaterer..." }
        try {
            roleMessageService.refresh(kord)
            logger.info("Role message refreshed by ${event.interaction.user.username}")
            response.edit { content = "Rolleoppdragsmelding er oppdatert." }
        } catch (e: Exception) {
            logger.error("Failed to refresh role message: ${e.message}", e)
            response.edit { content = "Noe gikk galt: ${e.message}" }
        }
    }
}
