package de.arondc.pipbot.automod

import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.events.NewAutoModPhraseEvent
import de.arondc.pipbot.events.TwitchMessageEvent
import mu.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class AutoModProcessor(
    val channelService: ChannelService,
    val autoModService: AutoModService
) {
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    fun receiveChatMessage(twitchMessageEvent: TwitchMessageEvent) {
        autoModService.processChat(
            channelService.findByNameIgnoreCase(twitchMessageEvent.channel)!!,
            twitchMessageEvent.user,
            twitchMessageEvent.message
        )
    }

    @ApplicationModuleListener
    fun receiveNewAutoModPhraseEvent(newAutoModPhraseEvent: NewAutoModPhraseEvent) {
        autoModService.processNewPhrase(
            channelService.findByNameIgnoreCase(newAutoModPhraseEvent.channel)!!,
            newAutoModPhraseEvent.newPhrase
        )
    }

}
