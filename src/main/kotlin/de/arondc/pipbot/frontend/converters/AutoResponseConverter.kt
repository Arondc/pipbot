package de.arondc.pipbot.frontend.converters

import de.arondc.pipbot.autoresponder.AutoResponseEntity
import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.frontend.dtos.AutoResponseDTO
import org.springframework.core.convert.converter.Converter


class AutoResponseEntityToDTOConverter : Converter<AutoResponseEntity, AutoResponseDTO> {
    override fun convert(source: AutoResponseEntity): AutoResponseDTO {
        return AutoResponseDTO(
            id = source.id,
            channel = source.channel!!.name,
            command = source.command,
            message = source.message,
        )
    }
}

class AutoResponseDTOToEntityConverter(val channelService: ChannelService) : Converter<AutoResponseDTO, AutoResponseEntity> {
    override fun convert(source: AutoResponseDTO): AutoResponseEntity {
        return AutoResponseEntity(
            channel = channelService.findByNameIgnoreCase(source.channel),
            command = source.command,
            message = source.message,
            id = source.id
        )
    }
}