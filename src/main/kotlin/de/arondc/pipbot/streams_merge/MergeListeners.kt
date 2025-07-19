package de.arondc.pipbot.streams_merge

import de.arondc.pipbot.events.*
import de.arondc.pipbot.streams.StreamServiceException
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class MergeListeners(
    val mergeService: MergeService,
    val eventPublisher: EventPublishingService
) {

    @ApplicationModuleListener
    fun receiveMessage(processingEvent: ProcessingEvent) {
        if (processingEvent.messageInfo.text.startsWith("!merge") && processingEvent.userInfo.permissions.satisfies(TwitchPermission.MODERATOR)) {
            try {
                mergeService.mergeStream(processingEvent.channel)
            } catch (e: StreamServiceException) {
                eventPublisher.publishEvent(
                    TwitchCallEvent(CallType.SEND_MESSAGE,processingEvent.channel, e.message ?: "Fehler")
                )
            }
        }
    }
}