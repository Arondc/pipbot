package de.arondc.pipbot.twitch

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.helix.domain.StreamList
import com.github.twitch4j.helix.domain.User
import mu.KotlinLogging
import org.springframework.stereotype.Service


@Service
class TwitchStreamService(
    val twitchClient: TwitchClient,
    val twitchConnectorConfig: OAuth2Credential
) {
    private val log = KotlinLogging.logger {}

    fun fetchStreamFromTwitch(channelName: String): StreamList {
        val token: String = twitchConnectorConfig.accessToken
        val numbersOfStreamsToFetch = 1
        val channelsToFetchStreamsFor = listOf(channelName)
        val execute = twitchClient.helix
            .getStreams(token, null, null, numbersOfStreamsToFetch, null, null, null, channelsToFetchStreamsFor)
            .execute()
        log.debug { "Found streams for $channelName - $execute" }
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

}