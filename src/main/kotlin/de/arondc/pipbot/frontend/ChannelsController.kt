package de.arondc.pipbot.frontend

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.ObjectError
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/channels")
class ChannelsController(val frontendService: FrontendService) {

    @ModelAttribute("channels")
    fun initChannelsList(): List<ChannelDTO> {
        return frontendService.getChannels()
    }

    @ModelAttribute("channel")
    fun initChannel(@RequestParam("channel-id") channelId: Long?): ChannelDTO {
        return if (channelId != null) {
            frontendService.getChannel(channelId)
        } else {
            ChannelDTO()
        }
    }

    @GetMapping("")
    fun viewChannels(model: Model): String {
        return "channels"
    }

    @PostMapping("/save")
    fun createNewChannel(
        @ModelAttribute newChannelInformation: ChannelDTO,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            if (newChannelInformation.id == null) {
                frontendService.createNewChannel(newChannelInformation)
            } else {
                frontendService.updateChannel(newChannelInformation)
            }
        } catch (frontendException: FrontendException) {
            bindingResult.addError(ObjectError("globalError", frontendException.message))
            //TODO low-prio : Gibt's das auch in schön?
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.channel", bindingResult)
            redirectAttributes.addFlashAttribute("channel", newChannelInformation)
        }
        return "redirect:/channels"
    }

    @GetMapping("/delete")
    fun deleteChannel(@RequestParam("channel-id") channelId: Long?): String {
        if (channelId != null) {
            frontendService.deleteChannel(channelId)
        }
        return "redirect:/channels"
    }

    @GetMapping("/deactivate")
    fun deactivateChannel(@RequestParam("channel-id") channelId: Long?) : String {
        if(channelId != null) {
            frontendService.deactivateChannel(channelId)
        }
        return "redirect:/channels"
    }

    @GetMapping("/activate")
    fun activateChannel(@RequestParam("channel-id") channelId: Long?) : String {
        if(channelId != null) {
            frontendService.activateChannel(channelId)
        }
        return "redirect:/channels"
    }
}