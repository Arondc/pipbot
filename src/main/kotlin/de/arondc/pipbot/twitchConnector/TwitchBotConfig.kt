package de.arondc.pipbot.twitchConnector

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TwitchBotConfig(val twitchConnectorConfig: TwitchConnectorConfig) {

    private val log = KotlinLogging.logger {}

    @Bean("twitchClient")
    fun twitchClient( twitchCredential: OAuth2Credential) : TwitchClient {
        log.info { "Preparing Twitch client" }
        val twitchClient = TwitchClientBuilder.builder()
            .withClientId(twitchConnectorConfig.clientId)
            .withClientSecret(twitchConnectorConfig.clientSecret)
            .withEnableHelix(true)
            .withChatCommandsViaHelix(true)
            .withChatAccount(twitchCredential)
            .withEnableChat(true)
            .withEnableGraphQL(true)
            .build()

        log.info { "Twitch client prepared" }
        return twitchClient
    }

    @Bean ("twitchCredential")
    fun twitchCredential(): OAuth2Credential = OAuth2Credential("twitch", twitchConnectorConfig.oAuth)

}