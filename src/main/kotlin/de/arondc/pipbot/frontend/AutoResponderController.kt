package de.arondc.pipbot.frontend

import de.arondc.pipbot.frontend.dtos.AutoResponseDTO
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.validation.BindingResult
import org.springframework.validation.ObjectError
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/autoresponder")
class AutoResponderController(private val frontendService: FrontendService) {

    @ModelAttribute("autoresponses")
    fun initAutoResponseList(): List<AutoResponseDTO> {
        return frontendService.getAutoResponses()
    }

    @ModelAttribute("autoresponse")
    fun initAutoResponse(@RequestParam("auto-response-id") autoResponseId: Long?): AutoResponseDTO {
        return if (autoResponseId != null) {
            frontendService.getAutoResponse(autoResponseId)
        } else {
            AutoResponseDTO()
        }
    }

    @GetMapping("")
    fun viewAutoresponses(model: Model): String {
        return "autoresponder"
    }

    @PostMapping("/save")
    fun createNewAutoResponse(
        @ModelAttribute autoResponseInformation: AutoResponseDTO,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            if (autoResponseInformation.id == null) {
                frontendService.createAutoResponse(autoResponseInformation)
            } else {
                frontendService.updateAutoResponse(autoResponseInformation)
            }
        } catch (frontendException: FrontendException) {
            bindingResult.addError(ObjectError("globalError", frontendException.message))
            addFlashAttributesToRedirect(redirectAttributes, bindingResult, autoResponseInformation, "autoresponse")
        }
        return "redirect:/autoresponder"
    }

    @GetMapping("/delete")
    fun deleteAutoResponse(@RequestParam("auto-response-id") autoResponseId: Long?): String {
        if (autoResponseId != null) {
            frontendService.deleteAutoResponse(autoResponseId)
        }
        return "redirect:/autoresponder"
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