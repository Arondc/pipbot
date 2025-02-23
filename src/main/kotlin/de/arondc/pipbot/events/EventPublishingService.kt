package de.arondc.pipbot.events

import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EventPublishingService(val applicationEventPublisher: ApplicationEventPublisher) {
    private val log = KotlinLogging.logger {}

    @Transactional
    fun publishEvent(event: PipBotEvent) {
        log.info { "Publishing event: $event" }
        applicationEventPublisher.publishEvent(event)
    }
}