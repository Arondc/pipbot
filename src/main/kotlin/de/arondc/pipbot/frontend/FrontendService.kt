package de.arondc.pipbot.frontend

import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.memes.MemeService
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class FrontendService(val memeService: MemeService, val channelService: ChannelService, val conversionService: ConversionService) {

    fun getMemes(streamId: Optional<Long> = Optional.empty()): List<MemeDTO> {
        var memes: List<MemeDTO> = listOf()
        streamId
            .ifPresentOrElse(
                { id ->
                    memes =
                        memeService.findByStreamId(id).mapNotNull { conversionService.convert(it, MemeDTO::class.java) }
                },
                { memes = memeService.findAll().mapNotNull { conversionService.convert(it, MemeDTO::class.java) } }
            )
        return memes
    }

    fun getChannels() : List<ChannelDTO> {
        val channels = channelService.findAll()
        return channels.mapNotNull { conversionService.convert(it, ChannelDTO::class.java) }
    }

    fun saveChannel(newChannel: ChannelDTO) {
        val channelEntity = conversionService.convert(newChannel, ChannelEntity::class.java)!!
        channelService.save(channelEntity)
        //TODO join newly configured channel
    }
}
