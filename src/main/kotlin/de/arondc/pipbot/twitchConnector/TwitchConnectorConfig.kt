package de.arondc.pipbot.twitchConnector

import de.arondc.pipbot.YamlPropertiesFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource


@Configuration
@PropertySource(value = ["file:authentication.yml"], factory = YamlPropertiesFactory::class)
@ConfigurationProperties(prefix = "twitch-connector.authentication")
data class TwitchConnectorConfig(
    var user: String = "",
    var oAuth: String = "",
    var clientSecret: String = "",
    var clientId: String = ""
)

@Configuration
@PropertySource(value = ["file:pipbot.yml"], factory = YamlPropertiesFactory::class)
@ConfigurationProperties(prefix = "twitch-connector.channels")
data class TwitchConnectorChannels(
    var channelNames : List<String> = listOf()
)