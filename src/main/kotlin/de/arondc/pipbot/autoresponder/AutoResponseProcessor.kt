package de.arondc.pipbot.autoresponder

import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchMessage
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class AutoResponseProcessor(val autoResponseService: AutoResponseService, val publisher: ApplicationEventPublisher) {
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    @Async
    fun respond(twitchMessage: TwitchMessage) {
        if(twitchMessage.message.startsWith("!")) {
            val response =
                autoResponseService.getAutoResponse(twitchMessage.channel, twitchMessage.message.substringAfter("!"))
            log.debug { "Autoresponder response: $response" }
            if(response != null) {
                publisher.publishEvent(
                    SendMessageEvent(
                        twitchMessage.channel,
                        response
                    )
                )
            }
        }
    }
}