package de.arondc.pipbot.streams

import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.helix.domain.StreamList
import de.arondc.pipbot.channels.ChannelService
import mu.KotlinLogging
import org.springframework.stereotype.Service


@Service
class TwitchStreamService(
    val streamRepository: StreamRepository,
    val channelService: ChannelService,
    val twitchClient: TwitchClient,
    val twitchConnectorConfig: OAuth2Credential
) {
    private val log = KotlinLogging.logger {}

    fun findCurrentStream(channelName: String): StreamEntity {
        val twitchStream = fetchStreamFromTwitch(channelName).streams.first()
        val channel = channelService.findOrCreate(channelName)
        return streamRepository.findByChannelAndStart(channel, twitchStream.startedAtInstant) ?: streamRepository.save(
            StreamEntity(twitchStream.startedAtInstant, channel)
        )
    }

    private fun fetchStreamFromTwitch(channelName: String): StreamList {
        val token: String = twitchConnectorConfig.accessToken
        val execute = twitchClient.helix
            .getStreams(token, null, null, 1, null, null, null, listOf(channelName))
            .execute()
        log.info { "Found streams for $channelName - $execute" }
        return execute
    }

}