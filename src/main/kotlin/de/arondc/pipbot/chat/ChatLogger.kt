package de.arondc.pipbot.chat

import de.arondc.pipbot.events.TwitchMessageEvent
import mu.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class ChatLogger {
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    fun logChat(twitchMessageEvent: TwitchMessageEvent) {
        log.info("Channel ${twitchMessageEvent.channel} - User ${twitchMessageEvent.user} - Message ${twitchMessageEvent.message} - Permissions ${twitchMessageEvent.permissions}")
    }

}