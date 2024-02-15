package de.arondc.pipbot.twitch

import com.github.philippheuer.events4j.simple.SimpleEventHandler
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.chat.events.channel.RaidEvent
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional


@Component
class TwitchConnector(
    val twitchClient: TwitchClient,
    val twitchConnectorChannels: TwitchConnectorChannels,
    val twitchConnectorPublisher: TwitchConnectorPublisher
) {
    private val log = KotlinLogging.logger {}

    @PostConstruct
    fun start() {
        log.info { "joining twitch channels: ${twitchConnectorChannels.channelNames}" }
        //TODO Read Channels from Database and join them instead of reading them from a file
        twitchConnectorChannels.channelNames.forEach {
            twitchClient.chat.joinChannel(it)
        }
        log.debug { "registering eventhandling for twitch" }

        twitchClient.eventManager.getEventHandler(SimpleEventHandler::class.java)
            .onEvent(ChannelMessageEvent::class.java, ::messageReceived)
        twitchClient.eventManager.getEventHandler(SimpleEventHandler::class.java)
            .onEvent(RaidEvent::class.java, ::raidEventReceived)
    }

    fun messageReceived(channelMessageEvent: ChannelMessageEvent) =
        twitchConnectorPublisher.publishMessage(channelMessageEvent)

    fun raidEventReceived(raidEvent: RaidEvent) = twitchConnectorPublisher.publishRaid(raidEvent)

    @ApplicationModuleListener
    @Async
    fun sendMessage(sendMessageEvent: SendMessageEvent) {
        log.debug { "sending twitch message $sendMessageEvent" }
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

    @Transactional
    fun publishRaid(raidEvent: RaidEvent){
        publisher.publishEvent(
            TwitchRaidEvent(
                raidEvent.channel.name,
                raidEvent.raider.name,
                raidEvent.viewers
            )
        )
    }
}

