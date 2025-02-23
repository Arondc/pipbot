package de.arondc.pipbot.streams

import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.twitch.TwitchStream
import de.arondc.pipbot.twitch.TwitchStreamService
import org.springframework.stereotype.Service

@Service
class StreamService(
    val streamRepository: StreamRepository,
    val twitchStreamService: TwitchStreamService,
    val channelService: ChannelService
) {
    fun findOrPersistCurrentStream(channelName: String): StreamEntity? {
        val twitchStream = twitchStreamService.fetchCurrentStreamForChannel(channelName) ?: return null
        return findOrCreateMatchingStream(twitchStream)
    }

    fun findOrCreateMatchingStream(twitchStream: TwitchStream): StreamEntity {
        val channel = channelService.findByNameIgnoreCase(twitchStream.userName)
        return streamRepository.findByChannelAndStartTimesContains(
            channel, twitchStream.startingTime
        ) ?: streamRepository.save(
            StreamEntity(
                setOf(twitchStream.startingTime), channel
            )
        )
    }

    fun findLatestStreamForChannel(channelName: String): StreamEntity? {
        return streamRepository.findAllByChannelName(channelName).maxByOrNull { it.startTimes.max() }
    }


    fun findAll(): List<StreamEntity> {
        return streamRepository.findAll()
    }

    fun save(streamEntity: StreamEntity) = streamRepository.save(streamEntity)
    fun delete(streamEntity: StreamEntity) = streamRepository.delete(streamEntity)
    fun findById(streamId: Long) = streamRepository.findById(streamId) //TODO Von Optional auf nullable umstellen
    fun deleteAll(streams: Iterable<StreamEntity>) = streamRepository.deleteAll(streams)
    fun findAllByChannelName(channelName: String) = streamRepository.findAllByChannelName(channelName)

}

class StreamServiceException(msg: String) : RuntimeException(msg)
