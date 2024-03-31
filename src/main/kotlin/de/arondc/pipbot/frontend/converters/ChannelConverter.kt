package de.arondc.pipbot.frontend.converters

import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.frontend.dtos.ChannelDTO
import org.springframework.core.convert.converter.Converter


class ChannelEntityToDTOConverter : Converter<ChannelEntity, ChannelDTO> {
    override fun convert(source: ChannelEntity): ChannelDTO {
        return ChannelDTO(
            source.id,
            source.name,
            source.language,
            source.active,
            source.shoutoutOnRaid,
            source.automatedShoutoutChannels
        )
    }
}

class ChannelDTOToEntityConverter : Converter<ChannelDTO, ChannelEntity> {
    override fun convert(source: ChannelDTO): ChannelEntity {
        return ChannelEntity(
            source.name,
            source.language,
            source.shoutOutOnRaidMode,
            source.shoutoutChannels,
            source.active,
            source.id
        )
    }
}