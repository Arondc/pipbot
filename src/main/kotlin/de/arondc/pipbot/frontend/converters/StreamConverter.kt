package de.arondc.pipbot.frontend.converters

import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.frontend.dtos.StreamDTO
import de.arondc.pipbot.streams.StreamEntity
import org.springframework.core.convert.converter.Converter


class StreamEntityToDTOConverter : Converter<StreamEntity, StreamDTO> {
    override fun convert(source: StreamEntity): StreamDTO {
        return StreamDTO(source.id!!, source.channel.name, source.startTimes.sorted())
    }
}

class StreamDTOToEntityConverter(private val channelService: ChannelService) : Converter<StreamDTO, StreamEntity> {
    override fun convert(source: StreamDTO): StreamEntity {
        return StreamEntity(source.startTimes.toSet(), channelService.findByNameIgnoreCase(source.channelName)!!)
    }
}