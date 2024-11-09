package de.arondc.pipbot.moderation.frontend

import de.arondc.pipbot.frontend.FrontendException
import de.arondc.pipbot.moderation.ModerationResponeType
import de.arondc.pipbot.moderation.UserTrustLevel
import mu.KotlinLogging
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.validation.ObjectError
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/moderationresponses")
class ModerationFrontendController(val frontendService: ModerationResponseFrontendService) {

    private val log = KotlinLogging.logger {}

    @ModelAttribute("userTrustLevelList")
    fun userTrustLevelList(): List<String> {
        return UserTrustLevel.entries.map { it.name }
    }

    @ModelAttribute("moderationResponeTypeList")
    fun moderationResponeType(): List<String> {
        return ModerationResponeType.entries.map { it.name }
    }

    @ModelAttribute("moderationResponses")
    fun initModerationResponseList(): List<ModerationResponseDTO> {
        return frontendService.getModerationResponses()
    }

    @ModelAttribute("moderationResponse")
    fun initModerationResponse() = ModerationResponseDTO()

    @ModelAttribute("channels")
    fun initChannelList(): List<ModerationChannelDTO> {
        return frontendService.getChannels()
    }

    @GetMapping("")
    fun viewModerationResponses() = "moderation_responses"

    @PostMapping("/save")
    fun createNewModerationResponse(
        @ModelAttribute moderationResponseDTO: ModerationResponseDTO,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            frontendService.createModerationResponse(moderationResponseDTO)
        } catch (frontendException: FrontendException) {
            log.error { frontendException }
            bindingResult.addError(ObjectError("globalError", frontendException.message))
            addFlashAttributesToRedirect(redirectAttributes, bindingResult, moderationResponseDTO, "moderationResponse")
        }
        return "redirect:/moderationresponses"
    }

    @GetMapping("/delete")
    fun deleteModerationResponse(@RequestParam("moderation-response-id") moderationResponseId: Long): String{
        frontendService.deleteModerationResponse(moderationResponseId)
        return "redirect:/moderationresponses"
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