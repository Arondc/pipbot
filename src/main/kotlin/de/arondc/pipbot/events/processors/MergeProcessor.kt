package de.arondc.pipbot.events.processors

import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchMessage
import de.arondc.pipbot.streams.StreamService
import de.arondc.pipbot.streams.StreamServiceException
import de.arondc.pipbot.twitch.TwitchPermission
import de.arondc.pipbot.twitch.satisfies
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class MergeProcessor(val streamService: StreamService, val applicationEventPublisher: ApplicationEventPublisher) {

    @ApplicationModuleListener
    fun receiveMessage(twitchMessage: TwitchMessage) {
        if (twitchMessage.message.startsWith("!merge") && twitchMessage.permissions.satisfies(TwitchPermission.MODERATOR)) {
            try {
                streamService.mergeStream(twitchMessage.channel)
            } catch (e: StreamServiceException) {
                applicationEventPublisher.publishEvent(
                    SendMessageEvent(twitchMessage.channel, e.message ?: "Fehler")
                )
            }
        }
    }
}