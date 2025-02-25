package de.arondc.pipbot.twitch

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.philippheuer.events4j.simple.SimpleEventHandler
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.chat.events.channel.RaidEvent
import com.github.twitch4j.common.exception.UnauthorizedException
import com.github.twitch4j.helix.domain.*
import com.netflix.hystrix.exception.HystrixRuntimeException
import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.events.*
import de.arondc.pipbot.twitch.domain.TwitchScope
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
    val channelService: ChannelService,
    val twitchConnectorConfig: OAuth2Credential
) {
    private val log = KotlinLogging.logger {}

    private val token by lazy { twitchConnectorConfig.accessToken }

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
            twitchConnectorPublisher.joinChannel(it)
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

    fun getStreams(channelNames: List<String>): StreamList {
        val numbersOfStreamsToFetch = channelNames.size
        return twitchClient.helix.getStreams(
            token,
            null,
            null,
            numbersOfStreamsToFetch,
            null,
            null,
            null,
            channelNames
        )
            .execute()
    }

    fun getChannelInformation(channelName: String): ChannelInformationList {
        val channelBroadcasterId = getUserInformation(channelName).users[0].id
        return twitchClient.helix
            .getChannelInformation(token, listOf(channelBroadcasterId))
            .execute()
    }

    fun getUserInformation(userName: String): UserList {
        return twitchClient.helix
            .getUsers(token, null, listOf(userName)).execute()
    }

    fun sendShoutout(fromChannel: String, toChannel: String) {
        val fromChannelId = getUserInformation(fromChannel).users[0].id
        val toChannelId = getUserInformation(toChannel).users[0].id
        val moderatorId = getUserInformation(twitchConnectorConfig.userName).users[0].id
        twitchClient.helix.sendShoutout(
            token,
            fromChannelId,
            toChannelId,
            moderatorId
        ).execute()
    }

    fun getChannelFollowers(channelName: String, userName: String): InboundFollowers {
        val channelBroadcasterId = getUserInformation(channelName).users[0].id
        val userId = getUserInformation(userName).users[0].id

        return try {
            twitchClient.helix.getChannelFollowers(
                twitchConnectorConfig.accessToken,
                channelBroadcasterId,
                userId,
                null,
                null
            ).execute()
        } catch (hre: HystrixRuntimeException) {
            throw when {
                hre.cause is UnauthorizedException && (hre.cause as UnauthorizedException).message.equals("missing scope ${TwitchScope.MODERATOR_READ_FOLLOWERS.scopeName}")
                    -> MissingScopeException(TwitchScope.MODERATOR_READ_FOLLOWERS)
                else -> TwitchConnectorException(hre)
            }
        }
    }

    fun getChatters(channelName: String): ChattersList {
        val channelBroadcasterId = getUserInformation(channelName).users[0].id
        val moderatorId = getUserInformation(twitchConnectorConfig.userName).users[0].id

        return twitchClient.helix.getChatters(
            twitchConnectorConfig.accessToken,
            channelBroadcasterId,
            moderatorId,
            null,
            null
        )
            .execute()
    }

    class MissingScopeException(val scope: TwitchScope) : RuntimeException()
    class TwitchConnectorException(cause: Throwable) : RuntimeException(cause)
}

@Component
class TwitchConnectorPublisher(val publisher: ApplicationEventPublisher) {
    private val log = KotlinLogging.logger {}

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

    @Transactional
    fun joinChannel(channel: String) {
        log.debug { "Sending event to join twitch channel $channel" }
        publisher.publishEvent(JoinTwitchChannelEvent(channel))
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
