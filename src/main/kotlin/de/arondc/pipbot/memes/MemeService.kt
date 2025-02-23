package de.arondc.pipbot.memes

import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.streams.StreamEntity
import de.arondc.pipbot.streams.StreamService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class MemeService(
    private val memeRepo: MemeRepository,
    private val memeBrowserSourceQueue: MemeBrowserSourceQueue,
    private val channelService: ChannelService,
    private val streamService: StreamService,
) {
    private val log = KotlinLogging.logger {}

    fun forwardMemeToBrowserSource(channelName: String, message: String){
        memeBrowserSourceQueue.queue(channelName, message)
    }

    fun save(memeEntity: MemeEntity): MemeEntity {
        return memeRepo.save(memeEntity)
    }

    fun findByStream(streamEntity: StreamEntity): Set<MemeEntity> {
        return memeRepo.findByStream(streamEntity)
    }

    fun findAll(): List<MemeEntity> {
        return memeRepo.findAll()
    }

    fun findByStreamId(id: Long): List<MemeEntity> {
        return memeRepo.findByStreamId(id)
    }

    fun processMemeMessage(channelName: String, user: String, message: String) {
        log.info { "Possible meme from $user detected: $message" }
        val channel = channelService.findByNameIgnoreCase(channelName)
        val memeEntity = MemeEntity(
            LocalDateTime.now(),
            channel,
            user,
            message,
            streamService.findOrPersistCurrentStream(channelName)
        )
        log.debug { memeEntity }
        memeRepo.save(memeEntity)
    }
}



