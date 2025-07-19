package de.arondc.pipbot.automod

import de.arondc.pipbot.events.*
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class AutoModListeners(
    val autoModService: AutoModService, val eventPublisher: EventPublishingService

) {

    @ApplicationModuleListener
    fun performPreCheck(autoModPreCheckEvent: AutoModPreCheckEvent) {
        val needsModeration = autoModService.needsModeration(
            autoModPreCheckEvent.channel, autoModPreCheckEvent.userInfo.userName, autoModPreCheckEvent.messageInfo
        )

        when (needsModeration) {
            true -> triggerModerationEvent(autoModPreCheckEvent)
            false -> triggerProcessingEvent(autoModPreCheckEvent)
        }
    }

    private fun triggerProcessingEvent(autoModPreCheckEvent: AutoModPreCheckEvent) {
        eventPublisher.publishEvent(
            ProcessingEvent(
                autoModPreCheckEvent.channel, autoModPreCheckEvent.messageInfo, autoModPreCheckEvent.userInfo
            )
        )
    }

    private fun triggerModerationEvent(autoModPreCheckEvent: AutoModPreCheckEvent) {
        eventPublisher.publishEvent(
            ModerationActionEvent(
                autoModPreCheckEvent.channel, autoModPreCheckEvent.userInfo.userName
            )
        )
    }

    @ApplicationModuleListener
    fun receiveNewAutoModPhraseEvent(newAutoModPhraseEvent: NewAutoModPhraseEvent) {

        val usersToModerate = autoModService.processNewPhrase(
            newAutoModPhraseEvent.channel, newAutoModPhraseEvent.newPhrase
        )

        usersToModerate.forEach { user ->
                eventPublisher.publishEvent(ModerationActionEvent(newAutoModPhraseEvent.channel, user))
            }
    }

}
