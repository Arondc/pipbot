package de.arondc.pipbot.twitch

import com.github.twitch4j.TwitchClient
import de.arondc.pipbot.events.CallType
import de.arondc.pipbot.events.TwitchCallEvent
import mu.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class TwitchListeners(val twitchClient: TwitchClient) {
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    @Async
    fun handleTwitchCallEvent(twitchCallEvent: TwitchCallEvent) {
        when(twitchCallEvent.callType) {
            CallType.SEND_MESSAGE -> {sendMessage(twitchCallEvent.channel, twitchCallEvent.message)}
            CallType.JOIN_CHANNEL -> {joinChannel(twitchCallEvent.channel)}
            CallType.LEAVE_CHANNEL -> {leaveChannel(twitchCallEvent.channel)}
        }
    }

    fun joinChannel(channelName : String) {
        log.info { "Joining twitch chat ${channelName}" }
        twitchClient.chat.joinChannel(channelName)
    }

    fun leaveChannel(channelName : String) {
        log.info { "Leaving twitch chat ${channelName}" }
        twitchClient.chat.leaveChannel(channelName)
    }

    fun sendMessage(channelName: String, message: String) {
        log.debug { "sending twitch message '$message' to channel '$channelName'" }
        twitchClient.chat.sendMessage(channelName, message)
    }
}