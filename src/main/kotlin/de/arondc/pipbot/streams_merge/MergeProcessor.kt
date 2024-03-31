package de.arondc.pipbot.streams_merge

import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchMessage
import de.arondc.pipbot.events.TwitchPermission
import de.arondc.pipbot.events.satisfies
import de.arondc.pipbot.streams.StreamServiceException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class MergeProcessor(val mergeService: MergeService, val applicationEventPublisher: ApplicationEventPublisher) {

    @ApplicationModuleListener
    fun receiveMessage(twitchMessage: TwitchMessage) {
        if (twitchMessage.message.startsWith("!merge") && twitchMessage.permissions.satisfies(TwitchPermission.MODERATOR)) {
            try {
                mergeService.mergeStream(twitchMessage.channel)
            } catch (e: StreamServiceException) {
                applicationEventPublisher.publishEvent(
                    SendMessageEvent(twitchMessage.channel, e.message ?: "Fehler")
                )
            }
        }
    }
}