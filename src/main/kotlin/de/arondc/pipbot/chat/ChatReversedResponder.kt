package de.arondc.pipbot.chat

import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchMessageEvent
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class ChatReversedResponder(val publisher: ApplicationEventPublisher) {
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    @Async
    fun respond(twitchMessageEvent: TwitchMessageEvent) {
        log.debug { "responding to message" }
        if (twitchMessageEvent.messageInfo.text.startsWith("!reverse ")) {
            publisher.publishEvent(
                SendMessageEvent(
                    twitchMessageEvent.channel,
                    twitchMessageEvent.messageInfo.text.substringAfter("!reverse ").reversed()
                )
            )
        }
    }
}