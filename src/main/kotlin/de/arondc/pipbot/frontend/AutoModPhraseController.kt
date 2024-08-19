package de.arondc.pipbot.frontend

import de.arondc.pipbot.frontend.dtos.AutoModChannelDTO
import de.arondc.pipbot.frontend.dtos.AutoModPhraseDTO
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.ObjectError
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/automodphrases")
class AutoModPhraseController(val frontendService: FrontendService) {

    @ModelAttribute("automodphrases")
    fun initAutoResponseList(): List<AutoModPhraseDTO> {
        return frontendService.getAutoModPhrases()
    }

    @ModelAttribute("channels")
    fun initChannelList(): List<AutoModChannelDTO> {
        return frontendService.getAutoModChannels()
    }

    @ModelAttribute("automodphrase")
    fun initAutoResponse() = AutoModPhraseDTO()

    @GetMapping("")
    fun viewAutoresponses(model: Model): String {
        return "automodphrases"
    }

    @PostMapping("/save")
    fun createNewAutoResponse(
        @ModelAttribute autoResponseInformation: AutoModPhraseDTO,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            frontendService.createAutoModPhrase(autoResponseInformation)
        } catch (frontendException: FrontendException) {
            bindingResult.addError(ObjectError("globalError", frontendException.message))
            addFlashAttributesToRedirect(redirectAttributes, bindingResult, autoResponseInformation, "automodphrase")
        }
        return "redirect:/automodphrases"
    }

    @GetMapping("/delete")
    fun deleteAutoResponse(@RequestParam("auto-mod-phrase-id") autoModPhraseId: Long?): String {
        if (autoModPhraseId != null) {
            frontendService.deleteAutoModPhrase(autoModPhraseId)
        }
        return "redirect:/automodphrases"
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