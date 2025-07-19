package de.arondc.pipbot.incoming

import de.arondc.pipbot.events.AutoModPreCheckEvent
import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.MessageEvent
import de.arondc.pipbot.userchannelinformation.UserChannelInformationService
import mu.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class IncomingMessageEventListener(
    val userChannelInformationService: UserChannelInformationService,
    val eventPublisher: EventPublishingService,
) {
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    fun handleUpdateChannelInformationForUserEvent(event: MessageEvent) {
        log.debug { "received event to update user channel information ${event.channel} - ${event.userInfo.userName} - ${event.userInfo.permissions} " }
        userChannelInformationService.updateChannelInformationForUser(
            event.channel,
            event.userInfo.userName,
            event.userInfo.permissions
        )

        eventPublisher.publishEvent(
            AutoModPreCheckEvent(event.channel, event.messageInfo, event.userInfo)
        )
    }
}