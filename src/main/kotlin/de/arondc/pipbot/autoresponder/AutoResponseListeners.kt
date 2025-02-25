package de.arondc.pipbot.autoresponder

import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchMessageEvent
import mu.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class AutoResponseListeners(
    val autoResponseService: AutoResponseService, val eventPublisher: EventPublishingService
) {
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    fun respond(twitchMessageEvent: TwitchMessageEvent) {
        if (twitchMessageEvent.messageInfo.text.startsWith("!")) {
            val response = autoResponseService.getAutoResponse(
                twitchMessageEvent.channel, twitchMessageEvent.messageInfo.text.substringAfter("!")
            )
            log.debug { "Autoresponder response: $response" }
            if (response != null) {
                eventPublisher.publishEvent(
                    SendMessageEvent(
                        twitchMessageEvent.channel, response
                    )
                )
            }
        }
    }
}