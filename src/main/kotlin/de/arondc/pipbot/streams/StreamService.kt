package de.arondc.pipbot.streams

import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.memes.MemeService
import de.arondc.pipbot.twitch.TwitchStreamService
import org.springframework.stereotype.Service
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
        val twitchStream = twitchStreamService.fetchStreamFromTwitch(channelName).streams.first()
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

    @Transactional
    fun mergeStream(targetStreamId : Long, sourceStreamId : Long) {
        if(targetStreamId == sourceStreamId){
            throw RuntimeException("target and source ids should be different")
        }

        val targetStream = streamRepository.findById(targetStreamId).orElseThrow()
        val sourceStream = streamRepository.findById(sourceStreamId).orElseThrow()

        if(targetStream.channel != sourceStream.channel){
            throw RuntimeException("target and source streams must belong to same channel")
        }
        val sourceStreamMemes = memeService.findByStream(sourceStream)

        sourceStreamMemes.map { me -> me.associateToNewStream(targetStream) }.forEach { memeService.save(it) }
        streamRepository.save(targetStream.associateAdditionalStartTime(sourceStream.startTimes))
        streamRepository.delete(sourceStream)

        targetStream.startTimes.sorted().first()

    }

}