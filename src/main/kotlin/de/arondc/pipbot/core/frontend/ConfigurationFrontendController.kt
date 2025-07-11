package de.arondc.pipbot.core.frontend

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/configuration")
class ConfigurationFrontendController(
    private val configurationFrontendService: ConfigurationFrontendService,
) {

    @ModelAttribute("configuration")
    fun configuration(): ConfigurationDTO {
        return configurationFrontendService.getConfiguration()
    }

    @GetMapping("")
    fun showConfiguration(): String = "configuration"

    @PostMapping("/save")
    fun saveConfiguration(@ModelAttribute configurationDTO: ConfigurationDTO): String {
        configurationFrontendService.updateConfiguration(configurationDTO.username,
            configurationDTO.oAuthToken,
            configurationDTO.clientId,
            configurationDTO.clientSecret)
        return "redirect:/configuration"
    }
}
