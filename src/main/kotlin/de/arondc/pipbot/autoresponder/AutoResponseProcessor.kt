package de.arondc.pipbot.autoresponder

import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchMessageEvent
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
    fun respond(twitchMessageEvent: TwitchMessageEvent) {
        if(twitchMessageEvent.messageInfo.text.startsWith("!")) {
            val response =
                autoResponseService.getAutoResponse(twitchMessageEvent.channel, twitchMessageEvent.messageInfo.text.substringAfter("!"))
            log.debug { "Autoresponder response: $response" }
            if(response != null) {
                publisher.publishEvent(
                    SendMessageEvent(
                        twitchMessageEvent.channel,
                        response
                    )
                )
            }
        }
    }
}