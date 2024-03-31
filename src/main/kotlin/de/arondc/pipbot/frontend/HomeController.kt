package de.arondc.pipbot.frontend

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class HomeController {
    @GetMapping("")
    fun home() = "home"


}
