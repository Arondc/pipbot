package de.arondc.pipbot.frontend.converters

import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.frontend.dtos.AutoModChannelDTO
import de.arondc.pipbot.frontend.dtos.ChannelDTO
import org.springframework.core.convert.converter.Converter


class ChannelEntityToDTOConverter : Converter<ChannelEntity, ChannelDTO> {
    override fun convert(source: ChannelEntity): ChannelDTO {
        return ChannelDTO(
            id = source.id,
            name = source.name,
            language = source.language,
            active = source.active,
            shoutOutOnRaidMode = source.shoutoutOnRaid,
            shoutoutChannels = source.automatedShoutoutChannels
        )
    }
}

//TODO : Besser!
class ChannelEntityToAutoModChannelDTOConverter : Converter<ChannelEntity, AutoModChannelDTO> {
    override fun convert(source: ChannelEntity): AutoModChannelDTO {
        return AutoModChannelDTO(
            name = source.name,
        )
    }
}

class ChannelDTOToEntityConverter : Converter<ChannelDTO, ChannelEntity> {
    override fun convert(source: ChannelDTO): ChannelEntity {
        return ChannelEntity(
            id = source.id,
            name = source.name,
            language = source.language,
            shoutoutOnRaid = source.shoutOutOnRaidMode,
            automatedShoutoutChannels = source.shoutoutChannels,
            active = source.active
        )
    }
}