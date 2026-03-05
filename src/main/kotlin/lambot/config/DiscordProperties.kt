package lambot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "discord")
class DiscordProperties {
    lateinit var token: String
    lateinit var guildId: String
    var allowedRoleIds: List<String> = emptyList()
    var roleMessageChannelId: String = ""
    var roleAssignments: List<RoleAssignment> = emptyList()

    class RoleAssignment {
        var emoji: String = ""
        var roleId: String = ""
    }
}
