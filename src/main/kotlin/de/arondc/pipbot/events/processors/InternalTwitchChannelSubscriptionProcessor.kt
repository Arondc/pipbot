package de.arondc.pipbot.events.processors

import com.github.twitch4j.TwitchClient
import de.arondc.pipbot.events.JoinTwitchChannelEvent
import de.arondc.pipbot.events.LeaveTwitchChannelEvent
import mu.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component

@Component
class InternalTwitchChannelSubscriptionProcessor(val twitchClient: TwitchClient) {
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
}