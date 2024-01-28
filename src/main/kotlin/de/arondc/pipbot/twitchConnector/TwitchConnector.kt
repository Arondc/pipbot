package de.arondc.pipbot.twitchConnector

import com.github.philippheuer.events4j.simple.SimpleEventHandler
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Service
class TwitchConnector(val twitchClient: TwitchClient, val twitchConnectorChannels: TwitchConnectorChannels) {
    private val log = KotlinLogging.logger {}
    fun start() {
        log.info { "joining twitch channels: ${twitchConnectorChannels.channelNames}" }
        twitchConnectorChannels.channelNames.forEach {
            twitchClient.chat.joinChannel(it)
        }
    }
}

@Component
class ChatLogHandler(val twitchClient: TwitchClient) {
    private val log = KotlinLogging.logger {}

    @PostConstruct
    fun registerHandler() {
        twitchClient.eventManager.getEventHandler(SimpleEventHandler::class.java)
            .onEvent(ChannelMessageEvent::class.java, ::onChannelMessage)
    }

    fun onChannelMessage(messageEvent: ChannelMessageEvent) = log.info(
        "Channel ${messageEvent.channel.name} - User ${messageEvent.user.name} - Message ${messageEvent.message} - ${messageEvent.permissions}"
    )
}