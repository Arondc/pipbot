package de.arondc.pipbot.frontend

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
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

    @GetMapping("/channels")
    fun configuration(model : Model) : String {
        model.addAttribute("channels", frontendService.getChannels())
        model.addAttribute("channel", ChannelDTO())
        return "channels"
    }

    @PostMapping("/channels/save")
    fun saveNewConfiguration(@ModelAttribute newChannel : ChannelDTO) : String {
        frontendService.saveChannel(newChannel)
        //TODO Error handling
        return "redirect:/channels"
    }

    //TODO update channel
    //TODO delete channel
    //TODO activate/deactivate bot for channel
}
