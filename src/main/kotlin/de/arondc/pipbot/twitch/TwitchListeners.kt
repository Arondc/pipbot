package de.arondc.pipbot.twitch

import com.github.twitch4j.TwitchClient
import de.arondc.pipbot.events.JoinTwitchChannelEvent
import de.arondc.pipbot.events.LeaveTwitchChannelEvent
import de.arondc.pipbot.events.SendMessageEvent
import mu.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class TwitchListeners(val twitchClient: TwitchClient) {
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    @Async
    fun joinChannel(joinTwitchChannelEvent: JoinTwitchChannelEvent) {
        log.info { "Joining twitch chat ${joinTwitchChannelEvent.channel}" }
        twitchClient.chat.joinChannel(joinTwitchChannelEvent.channel)
    }

    @ApplicationModuleListener
    @Async
    fun leaveChannel(leaveTwitchChannelEvent: LeaveTwitchChannelEvent) {
        log.info { "Leaving twitch chat ${leaveTwitchChannelEvent.channel}" }
        twitchClient.chat.leaveChannel(leaveTwitchChannelEvent.channel)
    }

    @ApplicationModuleListener
    @Async
    fun sendMessage(sendMessageEvent: SendMessageEvent) {
        log.debug { "sending twitch message $sendMessageEvent" }
        twitchClient.chat.sendMessage(sendMessageEvent.channel, sendMessageEvent.message)
    }
}