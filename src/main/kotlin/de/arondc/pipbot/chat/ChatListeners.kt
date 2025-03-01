package de.arondc.pipbot.chat

import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchMessageEvent
import mu.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class ChatListeners(private val eventPublisher: EventPublishingService) {
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    fun logChat(twitchMessageEvent: TwitchMessageEvent) {
        log.info("Channel ${twitchMessageEvent.channel} - User ${twitchMessageEvent.userInfo} - Message ${twitchMessageEvent.messageInfo.text} - Permissions ${twitchMessageEvent.userInfo.permissions}")
    }

    @ApplicationModuleListener
    fun respond(twitchMessageEvent: TwitchMessageEvent) {
        log.debug { "responding to message" }
        if (twitchMessageEvent.messageInfo.text.startsWith("!reverse ")) {
            eventPublisher.publishEvent(
                SendMessageEvent(
                    twitchMessageEvent.channel,
                    twitchMessageEvent.messageInfo.text.substringAfter("!reverse ").reversed()
                )
            )
        }
    }

}