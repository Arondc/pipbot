package de.arondc.pipbot.automod

import de.arondc.pipbot.events.NewAutoModPhraseEvent
import de.arondc.pipbot.events.ProcessingEvent
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class AutoModListeners(
    val autoModService: AutoModService
) {

    @ApplicationModuleListener
    fun receiveChatMessage(processingEvent: ProcessingEvent) {
        autoModService.processChat(
            processingEvent.channel,
            processingEvent.userInfo.userName,
            processingEvent.messageInfo
        )
    }

    @ApplicationModuleListener
    fun receiveNewAutoModPhraseEvent(newAutoModPhraseEvent: NewAutoModPhraseEvent) {
        autoModService.processNewPhrase(
            newAutoModPhraseEvent.channel,
            newAutoModPhraseEvent.newPhrase
        )
    }

}
