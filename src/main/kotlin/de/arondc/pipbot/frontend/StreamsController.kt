package de.arondc.pipbot.frontend

import de.arondc.pipbot.frontend.dtos.MergeInfoDTO
import de.arondc.pipbot.frontend.dtos.StreamDTO
import mu.KotlinLogging
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/streams")
class StreamsController(val frontendService: FrontendService) {
    private val log = KotlinLogging.logger {}

    @ModelAttribute("streams")
    fun initChannelsList(): List<StreamDTO> {
        return frontendService.getStreams().sortedBy { it.startTimes.min() }
    }

    @ModelAttribute("mergeInfo")
    fun initMergeInfo(): MergeInfoDTO {
        return MergeInfoDTO()
    }

    @GetMapping("")
    fun viewStreams(model: Model): String {
        return "streams"
    }

    @PostMapping("/merge")
    fun mergeStreams(@ModelAttribute mergeInfo: MergeInfoDTO): String {
        log.info { "merge info = $mergeInfo" }
        frontendService.mergeStreams(mergeInfo.streamIds)
        return "redirect:/streams"
    }
}

