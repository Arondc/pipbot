package de.arondc.pipbot.channels

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class ChannelService(private val channelRepository: ChannelRepository) {

    fun findByNameIgnoreCase(channelName: String): ChannelEntity {
        return channelRepository.findByNameIgnoreCase(channelName)
    }

    fun channelExists(channelName: String): Boolean {
        return channelRepository.existsByNameIgnoreCase(channelName)
    }

    fun findAll(): List<ChannelEntity> {
        return channelRepository.findAll()
    }

    fun findActive(): List<ChannelEntity> {
        return channelRepository.findAllByActiveIsTrue()
    }

    fun save(channelEntity: ChannelEntity): ChannelEntity {
        return channelRepository.save(channelEntity)
    }

    fun findById(channelId: Long): ChannelEntity? {
        return channelRepository.findByIdOrNull(channelId)
    }

    fun setActiveById(channelId: Long, active: Boolean) {
        channelRepository.setActiveById(channelId, active)
    }

    fun deleteChannel(channel: ChannelEntity) {
        channelRepository.delete(channel)
    }
}
