package de.arondc.pipbot.streams

import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.memes.MemeService
import de.arondc.pipbot.twitch.TwitchStreamService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneId

@Service
class StreamService(
    val streamRepository: StreamRepository,
    val twitchStreamService: TwitchStreamService,
    val channelService: ChannelService,
    val memeService: MemeService
) {
    fun findCurrentStream(channelName: String): StreamEntity {
        val twitchStream = try {
            twitchStreamService.fetchStreamFromTwitch(channelName).streams.first()
        } catch (_: NoSuchElementException) {
            throw StreamServiceException("No currently running stream found for $channelName")
        }
        val channel = channelService.findOrCreate(channelName)
        val startTime = LocalDateTime.ofInstant(twitchStream.startedAtInstant, ZoneId.systemDefault())
        return streamRepository.findByChannelAndStartTimesContains(
            channel, startTime
        ) ?: streamRepository.save(
            StreamEntity(
                setOf(startTime), channel
            )
        )
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun mergeStream(channelName: String) {
        val sourceStream = findCurrentStream(channelName)
        val targetStream = findLatestStreamBefore(sourceStream)
            ?: throw StreamServiceException("No stream before the currently running stream found")
        if (targetStream == sourceStream) {
            throw StreamServiceException("Target and source streams should be different")
        }

        val sourceStreamMemes = memeService.findByStream(sourceStream)
        sourceStreamMemes.map { me -> me.associateToNewStream(targetStream) }.forEach { memeService.save(it) }
        streamRepository.save(targetStream.associateAdditionalStartTime(sourceStream.startTimes))
        streamRepository.delete(sourceStream)
    }

    @Transactional
    fun mergeStreams(streamIds: List<Long>) {
        if (streamIds.size < 2) {
            throw RuntimeException("There should be more than one stream in the merge process")
        }
        val streams = streamIds.mapNotNull { streamRepository.findById(it).orElseThrow() }
        if (streams.map { it.channel.id }.count() > 1) {
            throw RuntimeException("The streams belong to more than one channel.")
        }

        val targetStream = streams.minBy { it.startTimes.min() }

        val sourceStreams = streams subtract setOf(targetStream)
        sourceStreams.flatMap { memeService.findByStream(it) }
            .map { me -> me.associateToNewStream(targetStream) }.forEach { memeService.save(it) }
        streamRepository.save(targetStream.associateAdditionalStartTime(sourceStreams.flatMap { it.startTimes }
            .toSet()))
        streamRepository.deleteAll(sourceStreams)
    }

    private fun findLatestStreamBefore(
        otherStream: StreamEntity
    ) = streamRepository.findAllByChannelName(otherStream.channel.name)
        .filter { !it.startTimes.any { startTime -> otherStream.startTimes.contains(startTime) } }
        .maxByOrNull { it.startTimes.max() }

    fun findAll(): List<StreamEntity> {
        return streamRepository.findAll()
    }


}

class StreamServiceException(msg: String) : RuntimeException(msg)
