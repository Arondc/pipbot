package de.arondc.pipbot.streams

import com.github.twitch4j.helix.domain.Stream
import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.twitch.TwitchStreamService
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class StreamService(
    val streamRepository: StreamRepository,
    val twitchStreamService: TwitchStreamService,
    val channelService: ChannelService
) {
    fun findOrPersistCurrentStream(channelName: String): StreamEntity? {
        val twitchStream = twitchStreamService.fetchCurrentStreamsForChannels(listOf(channelName)).streams.firstOrNull() ?: return null
        return findOrCreateMatchingStream(twitchStream)
    }

    fun findOrCreateMatchingStream(twitchStream: Stream): StreamEntity {
        val channel = channelService.findByNameIgnoreCase(twitchStream.userName)!!
        val startTime = LocalDateTime.ofInstant(twitchStream.startedAtInstant, ZoneId.systemDefault())
        return streamRepository.findByChannelAndStartTimesContains(
            channel, startTime
        ) ?: streamRepository.save(
            StreamEntity(
                setOf(startTime), channel
            )
        )
    }


    fun findAll(): List<StreamEntity> {
        return streamRepository.findAll()
    }

    fun save(streamEntity: StreamEntity) = streamRepository.save(streamEntity)
    fun delete(streamEntity: StreamEntity) = streamRepository.delete(streamEntity)
    fun findById(streamId: Long) = streamRepository.findById(streamId)
    fun deleteAll(streams: Iterable<StreamEntity>) = streamRepository.deleteAll(streams)
    fun findAllByChannelName(channelName: String) = streamRepository.findAllByChannelName(channelName)

}

class StreamServiceException(msg: String) : RuntimeException(msg)
