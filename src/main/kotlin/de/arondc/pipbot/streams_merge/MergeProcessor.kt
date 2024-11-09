package de.arondc.pipbot.streams_merge

import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchMessageEvent
import de.arondc.pipbot.events.TwitchPermission
import de.arondc.pipbot.events.satisfies
import de.arondc.pipbot.streams.StreamServiceException
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class MergeProcessor(val mergeService: MergeService, val applicationEventPublisher: ApplicationEventPublisher) {

    @ApplicationModuleListener
    fun receiveMessage(twitchMessageEvent: TwitchMessageEvent) {
        if (twitchMessageEvent.messageInfo.text.startsWith("!merge") && twitchMessageEvent.userInfo.permissions.satisfies(TwitchPermission.MODERATOR)) {
            try {
                mergeService.mergeStream(twitchMessageEvent.channel)
            } catch (e: StreamServiceException) {
                applicationEventPublisher.publishEvent(
                    SendMessageEvent(twitchMessageEvent.channel, e.message ?: "Fehler")
                )
            }
        }
    }
}