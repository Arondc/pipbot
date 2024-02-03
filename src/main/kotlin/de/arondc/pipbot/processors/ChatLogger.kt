package de.arondc.pipbot.processors

import de.arondc.pipbot.twitch.TwitchMessage
import mu.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class ChatLogger {
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    @Async
    fun logChat(twitchMessage: TwitchMessage) {
        log.info("Channel ${twitchMessage.channel} - User ${twitchMessage.user} - Message ${twitchMessage.message} - Permissions ${twitchMessage.permissions}")
    }

}