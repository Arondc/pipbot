package de.arondc.pipbot.core.frontend

import de.arondc.pipbot.core.ConfigurationService
import org.springframework.stereotype.Service

@Service
class ConfigurationFrontendService(private val configurationService: ConfigurationService) {
    fun updateConfiguration(username: String, oAuthToken: String, clientId: String, clientSecret: String) {
        configurationService.updateConfiguration(username, oAuthToken, clientId, clientSecret)
    }

    fun getConfiguration() : ConfigurationDTO {
        return ConfigurationDTO(
            username = configurationService.getUserName(),
            oAuthToken = configurationService.getOAuthToken(),
            clientId = configurationService.getClientId(),
            clientSecret = configurationService.getClientSecret()
        )
    }
}