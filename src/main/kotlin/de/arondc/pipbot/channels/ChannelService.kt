package de.arondc.pipbot.channels

import org.springframework.stereotype.Service
import java.util.Locale

@Service
class ChannelService(val channelRepository: ChannelRepository) {
    fun findOrCreate(channelName: String): ChannelEntity {
        return channelRepository.findByName(channelName) ?: channelRepository.save(
            ChannelEntity(
                channelName,
                Locale.GERMAN, ShoutOutOnRaidType.NONE, listOf()

            )
        )
    }

    fun findByName(channelName: String) : ChannelEntity{
        return channelRepository.findByName(channelName)!!
    }
}