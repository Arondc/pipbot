package de.arondc.pipbot.userchannelinformation

import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.ProcessingEvent
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class UserChannelInformationListener(
    val userChannelInformationService: UserChannelInformationService,
    val eventPublisher: EventPublishingService,
) {
    @ApplicationModuleListener
    fun receiveMessage(processingEvent: ProcessingEvent) {
        val message = processingEvent.messageInfo.text
        if (message.startsWith("!lastseen")) {
            val userName = message.substringAfter("!lastseen").trim()
                .ifEmpty { processingEvent.userInfo.userName }
            userChannelInformationService.handleLastSeen(userName, processingEvent.channel)
        }
    }
}
