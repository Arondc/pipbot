package de.arondc.pipbot.twitch

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.helix.domain.ChattersList
import com.github.twitch4j.helix.domain.StreamList
import com.github.twitch4j.helix.domain.User
import de.arondc.pipbot.events.JoinTwitchChannelEvent
import de.arondc.pipbot.events.LeaveTwitchChannelEvent
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant


@Service
class TwitchStreamService(
    val twitchClient: TwitchClient,
    val twitchConnectorConfig: OAuth2Credential,
    val publisher: ApplicationEventPublisher
) {
    private val log = KotlinLogging.logger {}

    fun fetchCurrentStreamsForChannels(channelNames: List<String>): StreamList {
        return fetchStreamsFromTwitch(channelNames)
    }

    private fun fetchStreamsFromTwitch(channelNames: List<String>): StreamList {
        val token: String = twitchConnectorConfig.accessToken
        val numbersOfStreamsToFetch = channelNames.size
        val execute = twitchClient.helix.getStreams(
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
        log.debug { "Found streams for $channelNames - $execute" }
        return execute
    }

    fun findLastGameFor(channelName: String): String {
        val channelBroadcasterId = getUserInformation(channelName).id
        val channelInformation = twitchClient.helix
            .getChannelInformation(twitchConnectorConfig.accessToken, listOf(channelBroadcasterId))
            .execute()
        return channelInformation.channels[0].gameName
    }

    private fun getUserInformation(userName: String): User {
        val userList = twitchClient.helix
            .getUsers(twitchConnectorConfig.accessToken, null, listOf(userName)).execute()
        return userList.users[0]
    }

    fun shoutout(raidedChannel: String, incomingRaid: String) {
        if (incomingRaid.isNotEmpty()) {
            val raidedChannelUserId = getUserInformation(raidedChannel).id
            val incomingRaiderUserId = getUserInformation(incomingRaid).id
            val moderatorId = getUserInformation(twitchConnectorConfig.userName).id
            twitchClient.helix.sendShoutout(
                twitchConnectorConfig.accessToken,
                raidedChannelUserId,
                incomingRaiderUserId,
                moderatorId
            ).execute()
        }
    }

    fun getFollowerInfoFor(channelName: String, userName: String): Instant? {
        val channelBroadcasterId = getUserInformation(channelName).id
        val userId = getUserInformation(userName).id
        val channelInfo = twitchClient.helix.getChannelFollowers(
            twitchConnectorConfig.accessToken,
            channelBroadcasterId,
            userId,
            null,
            null
        ).execute()

        return when {
            channelInfo.follows == null -> null
            channelInfo.follows!!.isEmpty() -> null
            else -> channelInfo.follows!![0].followedAt
        }
    }

    fun getChatUserList(channelName: String): ChattersList {
        val channelBroadcasterId = getUserInformation(channelName).id
        val moderatorId = getUserInformation(twitchConnectorConfig.userName).id
        val chatList = twitchClient.helix.getChatters(
            twitchConnectorConfig.accessToken,
            channelBroadcasterId,
            moderatorId,
            null,
            null
        )
            .execute()
        log.debug {chatList}
        return chatList
    }

    @Transactional
    fun joinChannel(channel: String) {
        log.debug { "Sending event to join twitch channel $channel" }
        publisher.publishEvent(JoinTwitchChannelEvent(channel))
    }

    @Transactional
    fun leaveChannel(channel: String) {
        log.debug { "Sending event to leave twitch channel $channel" }
        publisher.publishEvent(LeaveTwitchChannelEvent(channel))
    }
}