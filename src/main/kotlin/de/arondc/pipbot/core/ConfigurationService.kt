package de.arondc.pipbot.core

import de.arondc.pipbot.twitch.TwitchConnectorConfig
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class ConfigurationService(
    val configurationRepository: ConfigurationRepository,
    val twitchConnectorConfig: TwitchConnectorConfig,
) {
    private val log = KotlinLogging.logger {}

    init {
        initConfiguration()
    }

    fun updateConfiguration(username: String, oAuthToken: String, clientId: String, clientSecret: String) {
        val configuration = configurationRepository.findById(0).get()
        configuration.username = username
        configuration.oAuthToken = oAuthToken
        configuration.clientId = clientId
        configuration.clientSecret = clientSecret
        configurationRepository.save(configuration)
    }

    fun getUserName(): String {
        return configurationRepository.findById(0).get().username
    }

    fun getOAuthToken(): String {
        return configurationRepository.findById(0).get().oAuthToken
    }

    fun getClientSecret(): String {
        return configurationRepository.findById(0).get().clientSecret
    }

    fun getClientId(): String {
        return configurationRepository.findById(0).get().clientId
    }

    private fun initConfiguration() {
        if(twitchConnectorConfig.isNotBlank()){
            log.warn { "Configuration from authentication.yml was transferred to database." +
                    " The file format configuration will be deprecated with a future release." }
        }

        if(configurationRepository.findById(0).isEmpty) {
            configurationRepository.save(
                Configuration(
                    username = twitchConnectorConfig.user,
                    oAuthToken = twitchConnectorConfig.oAuth,
                    clientSecret = twitchConnectorConfig.clientSecret,
                    clientId = twitchConnectorConfig.clientId,
                )
            )
        }
    }
}