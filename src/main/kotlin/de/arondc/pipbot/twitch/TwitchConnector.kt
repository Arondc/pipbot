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
import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.JoinTwitchChannelEvent
import de.arondc.pipbot.events.TwitchRaidEvent
import de.arondc.pipbot.twitch.domain.TwitchScope
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class TwitchConnector(
    val twitchClient: TwitchClient,
    val channelService: ChannelService,
    val twitchConnectorConfig: OAuth2Credential,
    val eventPublisher: EventPublishingService,
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
            eventPublisher.publishEvent(JoinTwitchChannelEvent(it))
        }
    }

    fun messageReceived(channelMessageEvent: ChannelMessageEvent) =
        eventPublisher.publishEvent(channelMessageEvent.toTwitchMessageEvent())

    fun raidEventReceived(raidEvent: RaidEvent) =
        eventPublisher.publishEvent(
            TwitchRaidEvent(
                raidEvent.channel.name,
                raidEvent.raider.name,
                raidEvent.viewers
            )
        )

    fun getStreams(channelNames: List<String>): StreamList {
        log.info { "TwitchClient - Fetching streams for channels: $channelNames" }
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
        log.info { "TwitchClient - Fetching ChannelInformation for channel: $channelName" }
        val channelBroadcasterId = getUserInformation(channelName).users[0].id
        return twitchClient.helix
            .getChannelInformation(token, listOf(channelBroadcasterId))
            .execute()
    }

    fun getUserInformation(userName: String): UserList {
        log.info { "TwitchClient - Fetching UserInformation for user: $userName" }
        return twitchClient.helix
            .getUsers(token, null, listOf(userName)).execute()
    }

    fun sendShoutout(fromChannel: String, toChannel: String) {
        log.info { "TwitchClient - Sending shoutout from $fromChannel to $toChannel" }
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
        log.info { "TwitchClient - Fetching followers of $channelName for $userName" }
        val channelBroadcasterId = getUserInformation(channelName).users[0].id
        val moderatorId = getUserInformation(userName).users[0].id

        return try {
            twitchClient.helix.getChannelFollowers(
                twitchConnectorConfig.accessToken,
                channelBroadcasterId,
                moderatorId,
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
        log.info { "TwitchClient - Fetching chatters for $channelName" }
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

