package de.arondc.pipbot.core.frontend

data class ConfigurationDTO(
    val username: String = "",
    val oAuthToken: String = "",
    val clientId: String = "",
    val clientSecret: String = "",
)