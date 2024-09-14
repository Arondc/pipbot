package de.arondc.pipbot.twitch

import com.github.philippheuer.events4j.simple.SimpleEventHandler
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.chat.events.channel.RaidEvent
import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.events.*
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.text.Normalizer


@Component
class TwitchConnector(
    val twitchClient: TwitchClient,
    val twitchConnectorPublisher: TwitchConnectorPublisher,
    val twitchStreamService: TwitchStreamService,
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
    fun runAfterApplicationStarted(event: ApplicationReadyEvent) {
        val channels = channelService.findActive().map { it.name }
        log.info { "joining twitch channels: $channels" }
        channels.forEach {
            twitchStreamService.joinChannel(it)
        }
    }

    fun messageReceived(channelMessageEvent: ChannelMessageEvent) =
        twitchConnectorPublisher.publishMessage(channelMessageEvent)
        //TODO subscriberMonths für die UserList mit abgreifen!

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
            TwitchMessageEvent(
                channelMessageEvent.channel.name,
                EventUserInfo(
                    channelMessageEvent.user.name,
                    channelMessageEvent.permissions.map { TwitchPermission.valueOf(it.name) }.toSet()
                ),
                EventMessageInfo(
                    channelMessageEvent.message,
                    normalizeMessage(channelMessageEvent.message),
                    messageContainsLink(channelMessageEvent.message),
                )
            )
        )
    }

    private fun normalizeMessage(message: String) =
        Normalizer.normalize(message, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")


    private fun messageContainsLink(message: String): Boolean {
        val couldBeATopLevelDomainEnding = """\S+\.\S{2,3}\b"""
        return message.contains("""http(s)?://""".toRegex()) ||
                message.contains(couldBeATopLevelDomainEnding.toRegex())
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
