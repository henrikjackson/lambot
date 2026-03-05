package lambot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "features")
class FeaturesProperties {
    var commands: Boolean = true
    var autoCreateEvents: Boolean = true
    var reactionRoles: Boolean = true
}
