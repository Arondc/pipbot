package de.arondc.pipbot.incoming

import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.ProcessingEvent
import de.arondc.pipbot.events.TwitchMessageEvent
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
    fun handleUpdateChannelInformationForUserEvent(event: TwitchMessageEvent) {
        log.debug { "received event to update user channel information ${event.channel} - ${event.userInfo.userName} - ${event.userInfo.permissions} " }
        userChannelInformationService.updateChannelInformationForUser(
            event.channel,
            event.userInfo.userName,
            event.userInfo.permissions
        )

        eventPublisher.publishEvent(
            ProcessingEvent(event.channel, event.messageInfo, event.userInfo)
        )
    }
}