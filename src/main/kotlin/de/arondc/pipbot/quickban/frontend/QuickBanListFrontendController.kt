package de.arondc.pipbot.quickban.frontend

import de.arondc.pipbot.frontend.FrontendException
import mu.KotlinLogging
import org.springframework.stereotype.Controller
import org.springframework.validation.BindingResult
import org.springframework.validation.ObjectError
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.mvc.support.RedirectAttributes

@Controller
@RequestMapping("/quickban")
class QuickBanListFrontendController(private val frontendService: QuickBanFrontendService) {

    private val log = KotlinLogging.logger {}

    @ModelAttribute("quickBanRequest")
    fun quickBanRequestDTO() = QuickBanRequestDTO()

    @ModelAttribute("channels")
    fun quickBanChannelListDTO() = frontendService.getChannels()

    @GetMapping("")
    fun viewQuickBan() = "quickban"

    @PostMapping("/ban")
    fun requestBanForUsers(
        @ModelAttribute quickBanList: QuickBanRequestDTO,
        bindingResult: BindingResult,
        redirectAttributes: RedirectAttributes
    ): String {
        try {
            frontendService.quickBan(quickBanList)
        } catch (frontendException: FrontendException) {
            log.error { frontendException }
            bindingResult.addError(ObjectError("globalError", frontendException.message))
            addFlashAttributesToRedirect(redirectAttributes, bindingResult, quickBanList, "quickBanList")
        }
        return "redirect:/quickban"
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