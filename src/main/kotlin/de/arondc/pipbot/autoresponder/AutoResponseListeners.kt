package de.arondc.pipbot.autoresponder

import de.arondc.pipbot.events.CallType
import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.ProcessingEvent
import de.arondc.pipbot.events.TwitchCallEvent
import mu.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class AutoResponseListeners(
    val autoResponseService: AutoResponseService, val eventPublisher: EventPublishingService
) {
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    fun respond(processingEvent: ProcessingEvent) {
        if (processingEvent.messageInfo.text.startsWith("!")) {
            val response = autoResponseService.getAutoResponse(
                processingEvent.channel, processingEvent.messageInfo.text.substringAfter("!")
            )
            log.debug { "Autoresponder response: $response" }
            if (response != null) {
                eventPublisher.publishEvent(
                    TwitchCallEvent(
                        CallType.SEND_MESSAGE,
                        processingEvent.channel, response
                    )
                )
            }
        }
    }
}