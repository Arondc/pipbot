package de.arondc.pipbot.frontend

import de.arondc.pipbot.frontend.dtos.ChannelDTO
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.ObjectError
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/channels")
class ChannelsController(private val frontendService: FrontendService) {

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
            addFlashAttributesToRedirect(redirectAttributes, bindingResult, newChannelInformation, "channel")
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
    fun deactivateChannel(@RequestParam("channel-id") channelId: Long?): String {
        if (channelId != null) {
            frontendService.deactivateChannel(channelId)
        }
        return "redirect:/channels"
    }

    @GetMapping("/activate")
    fun activateChannel(@RequestParam("channel-id") channelId: Long?): String {
        if (channelId != null) {
            frontendService.activateChannel(channelId)
        }
        return "redirect:/channels"
    }

    private fun addFlashAttributesToRedirect(
        redirectAttributes: RedirectAttributes,
        bindingResult: BindingResult,
        formObject: Any,
        formObjectName: String
    ) {
        redirectAttributes.addFlashAttribute(
            "org.springframework.validation.BindingResult.$formObjectName",
            bindingResult
        )
        redirectAttributes.addFlashAttribute(formObjectName, formObject)
    }
}