package de.arondc.pipbot.moderation

import de.arondc.pipbot.events.ModerationActionEvent
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class ModerationListeners(private val moderationService: ModerationService) {

    @ApplicationModuleListener
    fun processModerationActionEvent(event: ModerationActionEvent) {
        moderationService.moderate(event)
    }
}