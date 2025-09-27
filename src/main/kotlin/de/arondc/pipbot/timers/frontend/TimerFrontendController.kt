package de.arondc.pipbot.timers.frontend

import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.timers.TimerService
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/timers")
class TimerFrontendController(
    private val timerFrontendService: TimerFrontendService
) {

    @ModelAttribute("channels")
    fun initChannelsList(): List<String> {
        return timerFrontendService.getChannels()
    }

    @ModelAttribute("timerInfo")
    fun formObject(): TimerInfoDTO{
        return TimerInfoDTO()
    }

    @GetMapping
    fun overview() = "timers"

    @PostMapping("/start")
    fun startTimer(timerInfo: TimerInfoDTO): String {
        timerFrontendService.startTimer(timerInfo)
        return "timers"
    }
}

@Service
class TimerFrontendService(
    private val timerService: TimerService,
    private val channelService: ChannelService
    ){
    fun startTimer(timerInfo: TimerInfoDTO) {
        timerService.createTimer("",timerInfo.channel)
    }

    fun getChannels(): List<String> = channelService.findAll().map { it.name }
}

class TimerInfoDTO(
    val channel: String = ""
)