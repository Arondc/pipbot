package de.arondc.pipbot.automod

import de.arondc.pipbot.events.NewAutoModPhraseEvent
import de.arondc.pipbot.events.TwitchMessageEvent
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class AutoModListeners(
    val autoModService: AutoModService
) {

    @ApplicationModuleListener
    fun receiveChatMessage(twitchMessageEvent: TwitchMessageEvent) {
        autoModService.processChat(
            twitchMessageEvent.channel,
            twitchMessageEvent.userInfo.userName,
            twitchMessageEvent.messageInfo
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
