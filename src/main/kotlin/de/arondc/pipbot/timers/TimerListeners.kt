package de.arondc.pipbot.timers

import de.arondc.pipbot.events.ProcessingEvent
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class TimerListeners(
    private val timerService: TimerService,
) {
    @ApplicationModuleListener
    fun receiveMessage(processingEvent: ProcessingEvent) {
        if (processingEvent.messageInfo.text.startsWith("!timer", true)) {
            timerService.createTimer(
                processingEvent.messageInfo.text.substringAfter("!timer").trim(),
                processingEvent.channel
            )
        }
    }
}