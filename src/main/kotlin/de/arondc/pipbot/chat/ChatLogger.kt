package de.arondc.pipbot.chat

import de.arondc.pipbot.events.TwitchMessage
import mu.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class ChatLogger {
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    fun logChat(twitchMessage: TwitchMessage) {
        log.info("Channel ${twitchMessage.channel} - User ${twitchMessage.user} - Message ${twitchMessage.message} - Permissions ${twitchMessage.permissions}")
    }

}