package de.arondc.pipbot.chat

import de.arondc.pipbot.events.CallType
import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.ProcessingEvent
import de.arondc.pipbot.events.TwitchCallEvent
import mu.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class ChatListeners(private val eventPublisher: EventPublishingService) {
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    fun logChat(processingEvent: ProcessingEvent) {
        log.info("Channel ${processingEvent.channel} - User ${processingEvent.userInfo} - Message ${processingEvent.messageInfo.text} - Permissions ${processingEvent.userInfo.permissions}")
    }

    @ApplicationModuleListener
    fun respond(processingEvent: ProcessingEvent) {
        log.debug { "responding to message" }
        if (processingEvent.messageInfo.text.startsWith("!reverse ")) {
            eventPublisher.publishEvent(
                TwitchCallEvent(
                    CallType.SEND_MESSAGE,
                    processingEvent.channel,
                    processingEvent.messageInfo.text.substringAfter("!reverse ").reversed()
                )
            )
        }
    }

}