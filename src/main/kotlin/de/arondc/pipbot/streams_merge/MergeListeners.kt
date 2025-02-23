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
    fun receiveMessage(twitchMessageEvent: TwitchMessageEvent) {
        if (twitchMessageEvent.messageInfo.text.startsWith("!merge") && twitchMessageEvent.userInfo.permissions.satisfies(TwitchPermission.MODERATOR)) {
            try {
                mergeService.mergeStream(twitchMessageEvent.channel)
            } catch (e: StreamServiceException) {
                eventPublisher.publishEvent(
                    SendMessageEvent(twitchMessageEvent.channel, e.message ?: "Fehler")
                )
            }
        }
    }
}