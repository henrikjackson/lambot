package lambot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "raidhelper")
class RaidHelperProperties {
    lateinit var apiKey: String
    var baseUrl: String = "https://raid-helper.dev/api"
    lateinit var serverId: String
    lateinit var wednesdayChannelId: String
    lateinit var sundayChannelId: String
    lateinit var leaderId: String
    lateinit var templateId: String
}
