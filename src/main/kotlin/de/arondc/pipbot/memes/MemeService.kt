package de.arondc.pipbot.memes

import de.arondc.pipbot.streams.StreamEntity
import org.springframework.stereotype.Service

@Service
class MemeService(
    val memeRepo: MemeRepository,
) {
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
}



