package de.arondc.pipbot.processors

import de.arondc.pipbot.twitch.TwitchMessage
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ChatLogger {
    private val log = KotlinLogging.logger {}

    @EventListener
    fun logChat(twitchMessage: TwitchMessage) {
        log.info("Channel ${twitchMessage.channel} - User ${twitchMessage.user} - Message ${twitchMessage.message} - Permissions ${twitchMessage.permissions}")
    }

}