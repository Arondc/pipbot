package de.arondc.pipbot.twitch

import com.github.philippheuer.events4j.simple.SimpleEventHandler
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.chat.events.channel.RaidEvent
import de.arondc.pipbot.channels.ChannelService
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional


@Component
class TwitchConnector(
    val twitchClient: TwitchClient,
    val twitchConnectorPublisher: TwitchConnectorPublisher,
    val botTwitchConnector:BotTwitchConnector,
    val channelService: ChannelService
) {
    private val log = KotlinLogging.logger {}

    @PostConstruct
    fun start() {
        log.info { "registering eventhandling for twitch" }
        twitchClient.eventManager.getEventHandler(SimpleEventHandler::class.java)
            .onEvent(ChannelMessageEvent::class.java, ::messageReceived)
        twitchClient.eventManager.getEventHandler(SimpleEventHandler::class.java)
            .onEvent(RaidEvent::class.java, ::raidEventReceived)
    }

    @EventListener
    fun runAfterApplicationStarted(event : ApplicationReadyEvent) {
        val channels = channelService.findActive().map { it.name }
        log.info { "joining twitch channels: $channels" }
        channels.forEach {
            botTwitchConnector.joinTwitchChannel(it)
        }
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
    fun publishRaid(raidEvent: RaidEvent) {
        publisher.publishEvent(
            TwitchRaidEvent(
                raidEvent.channel.name,
                raidEvent.raider.name,
                raidEvent.viewers
            )
        )
    }
}

//TODO bessere Namen überlegen!!
@Component
class BotTwitchConnector(val publisher: ApplicationEventPublisher) {
    private val log = KotlinLogging.logger {}
    @Transactional
    fun joinTwitchChannel(channel : String){
        log.debug { "Sending event to join twitch channel $channel" }
        publisher.publishEvent(BotJoinTwitchChannelEvent(channel))
    }

    @Transactional
    fun leaveTwitchChannel(channel: String) {
        log.debug { "Sending event to leave twitch channel $channel" }
        publisher.publishEvent(BotLeaveTwitchChannelEvent(channel))
    }
}

