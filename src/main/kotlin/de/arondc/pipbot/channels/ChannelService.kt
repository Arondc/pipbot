package de.arondc.pipbot.channels

import org.springframework.stereotype.Service

@Service
class ChannelService(val channelRepository: ChannelRepository) {
    fun findOrCreate(channelName: String): ChannelEntity {
        return channelRepository.findByName(channelName) ?: channelRepository.save(ChannelEntity(channelName))
    }
}