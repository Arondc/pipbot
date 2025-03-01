package de.arondc.pipbot.channels

import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.JoinTwitchChannelEvent
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class ChannelListener(
    private val channelService: ChannelService,
    private val eventPublishingService: EventPublishingService
) {
    private val log = KotlinLogging.logger {}

    @EventListener
    fun runAfterApplicationStarted(event: ApplicationReadyEvent) {
        val channels = channelService.findActive().map { it.name }
        log.info { "joining twitch channels: $channels" }
        channels.forEach {
            eventPublishingService.publishEvent(JoinTwitchChannelEvent(it))
        }
    }
}
