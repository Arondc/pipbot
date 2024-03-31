package de.arondc.pipbot.frontend

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/memes")
class MemesController(val frontendService: FrontendService) {
    @GetMapping("")
    fun memes(model: Model, @RequestParam("stream-id") streamId: Long?): String {
        model.addAttribute("memes", frontendService.getMemes(streamId))
        return "memes"
    }
}
