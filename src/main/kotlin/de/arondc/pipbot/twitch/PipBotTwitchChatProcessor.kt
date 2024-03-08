package de.arondc.pipbot.twitch

import com.github.twitch4j.TwitchClient
import mu.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class PipBotTwitchChatProcessor(val twitchClient: TwitchClient) {
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    @Async
    fun joinChannel(joinTwitchChannelEvent: BotJoinTwitchChannelEvent) {
        log.info { "Joining twitch chat ${joinTwitchChannelEvent.channel}" }
        twitchClient.chat.joinChannel(joinTwitchChannelEvent.channel)
    }

    @ApplicationModuleListener
    @Async
    fun leaveChannel(leaveTwitchChannelEvent: BotLeaveTwitchChannelEvent) {
        log.info { "Leaving twitch chat ${leaveTwitchChannelEvent.channel}" }
        twitchClient.chat.leaveChannel(leaveTwitchChannelEvent.channel)
    }
}