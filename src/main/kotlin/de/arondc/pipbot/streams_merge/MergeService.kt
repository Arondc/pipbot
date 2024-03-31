package de.arondc.pipbot.streams_merge

import de.arondc.pipbot.memes.MemeService
import de.arondc.pipbot.streams.StreamEntity
import de.arondc.pipbot.streams.StreamService
import de.arondc.pipbot.streams.StreamServiceException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class MergeService(val streamService: StreamService, val memeService: MemeService) {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    fun mergeStream(channelName: String) {
        val sourceStream = streamService.findCurrentStream(channelName)
        val targetStream = findLatestStreamBefore(sourceStream)
            ?: throw StreamServiceException("No stream before the currently running stream found")
        if (targetStream == sourceStream) {
            throw StreamServiceException("Target and source streams should be different")
        }

        val sourceStreamMemes = memeService.findByStream(sourceStream)
        sourceStreamMemes.map { me -> me.associateToNewStream(targetStream) }.forEach { memeService.save(it) }
        streamService.save(targetStream.associateAdditionalStartTime(sourceStream.startTimes))
        streamService.delete(sourceStream)
    }

    @Transactional
    fun mergeStreams(streamIds: List<Long>) {
        if (streamIds.size < 2) {
            throw RuntimeException("There should be more than one stream in the merge process")
        }
        val streams = streamIds.mapNotNull { streamService.findById(it).orElseThrow() }
        if (streams.map { it.channel.id }.count() > 1) {
            throw RuntimeException("The streams belong to more than one channel.")
        }

        val targetStream = streams.minBy { it.startTimes.min() }

        val sourceStreams = streams subtract setOf(targetStream)
        sourceStreams.flatMap { memeService.findByStream(it) }
            .map { me -> me.associateToNewStream(targetStream) }.forEach { memeService.save(it) }
        streamService.save(targetStream.associateAdditionalStartTime(sourceStreams.flatMap { it.startTimes }
            .toSet()))
        streamService.deleteAll(sourceStreams)
    }

    private fun findLatestStreamBefore(
        otherStream: StreamEntity
    ) = streamService.findAllByChannelName(otherStream.channel.name)
        .filter { !it.startTimes.any { startTime -> otherStream.startTimes.contains(startTime) } }
        .maxByOrNull { it.startTimes.max() }
}