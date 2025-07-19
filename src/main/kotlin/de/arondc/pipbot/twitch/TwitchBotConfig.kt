package de.arondc.pipbot.twitch

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import de.arondc.pipbot.core.ConfigurationService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TwitchBotConfig(val configurationService: ConfigurationService) {
    @Bean("twitchClient")
    fun twitchClient(twitchCredential: OAuth2Credential): TwitchClient = TwitchClientBuilder.builder()
        .withClientId(configurationService.getClientId())
        .withClientSecret(configurationService.getClientId())
        .withEnableHelix(true)
        .withChatCommandsViaHelix(true)
        .withChatAccount(twitchCredential)
        .withEnableChat(true)
        .withEnableGraphQL(true)
        .build()

    @Bean("twitchCredential")
    fun twitchCredential(): OAuth2Credential = OAuth2Credential("twitch", configurationService.getOAuthToken())

}