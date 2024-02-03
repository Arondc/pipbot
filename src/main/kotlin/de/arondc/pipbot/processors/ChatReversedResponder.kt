package de.arondc.pipbot.processors

import de.arondc.pipbot.twitch.SendMessageEvent
import de.arondc.pipbot.twitch.TwitchMessage
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ChatReversedResponder(val publisher: ApplicationEventPublisher) {
    private val log = KotlinLogging.logger {}

    @EventListener
    fun respond(twitchMessage: TwitchMessage) {
        log.debug { "responding to message" }
        if (twitchMessage.message.startsWith("!reverse ")) {
            publisher.publishEvent(
                SendMessageEvent(
                    twitchMessage.channel,
                    twitchMessage.message.substringAfter("!reverse ").reversed()
                )
            )
        }
    }

}