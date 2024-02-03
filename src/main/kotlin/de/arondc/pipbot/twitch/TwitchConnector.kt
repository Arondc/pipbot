package de.arondc.pipbot.twitch

import com.github.philippheuer.events4j.simple.SimpleEventHandler
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class TwitchConnector(
    val twitchClient: TwitchClient,
    val twitchConnectorChannels: TwitchConnectorChannels,
    val twitchConnectorPublisher: TwitchConnectorPublisher
) {
    private val log = KotlinLogging.logger {}

    @PostConstruct
    fun start() {
        log.info { "joining twitch channels: ${twitchConnectorChannels.channelNames}" }
        twitchConnectorChannels.channelNames.forEach {
            twitchClient.chat.joinChannel(it)
        }
        twitchClient.eventManager.getEventHandler(SimpleEventHandler::class.java)
            .onEvent(ChannelMessageEvent::class.java, ::messageReceived)
    }

    fun messageReceived(channelMessageEvent: ChannelMessageEvent) =
        twitchConnectorPublisher.publishMessage(channelMessageEvent)

    @ApplicationModuleListener
    @Async
    fun sendMessage(sendMessageEvent: SendMessageEvent) {
        twitchClient.chat.sendMessage(sendMessageEvent.channel, sendMessageEvent.message)
    }
}

@Component
class TwitchConnectorPublisher(val publisher: ApplicationEventPublisher) {
    @Transactional
    fun publishMessage(channelMessageEvent: ChannelMessageEvent) {
        publisher.publishEvent(
            TwitchMessage(
                channelMessageEvent.channel.name,
                channelMessageEvent.user.name,
                channelMessageEvent.message,
                channelMessageEvent.permissions.map { it.name }.toSet()
            )
        )
    }
}

