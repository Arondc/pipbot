package de.arondc.pipbot.frontend

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.Optional

@Controller
class FrontendController(val frontendService: FrontendService) {
    @GetMapping("")
    fun home() = "home"

    @GetMapping("/memes")
    fun memes(model: Model, @RequestParam("stream-id") streamId: Optional<Long>): String {
        model.addAttribute("memes", frontendService.getMemes(streamId))

        return "memes"
    }
}
