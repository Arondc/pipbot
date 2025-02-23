package de.arondc.pipbot.twitch

import de.arondc.pipbot.twitch.domain.TwitchScope
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.client.RestClient

@Controller
class OauthTokenReceiverController(val twitchConnectorConfig: TwitchConnectorConfig) {
    companion object{
        private val scopes =
            TwitchScope.entries.map { it.scopeName }.toList()

        const val BASE_URL = "https://id.twitch.tv/oauth2/authorize"

        val requestParamMap =
            mapOf("response_type" to "token", "redirect_uri" to "http://localhost:8080/authtoken")

        fun buildScopeQueryParam() = scopes.joinToString(separator = " ")
    }



    @GetMapping("/authtoken")
    fun receiveAuthTokenRedirect(): String {
        println("authtoken page loading")
        return "authtoken"
    }

    @GetMapping("/authtoken/refresh")
    @ResponseBody
    fun refreshAuthTokenRedirect() =
        RestClient.builder().build()
            .get()
            .uri("$BASE_URL?response_type={response_type}&client_id={client_id}&redirect_uri={redirect_uri}&scope={scopes}",
                requestParamMap["response_type"],
                twitchConnectorConfig.clientId,
                requestParamMap["redirect_uri"],
                buildScopeQueryParam())
            .retrieve()
            .toBodilessEntity()
}
