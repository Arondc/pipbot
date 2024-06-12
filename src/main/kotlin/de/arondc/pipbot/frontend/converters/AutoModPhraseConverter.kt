package de.arondc.pipbot.frontend.converters

import de.arondc.pipbot.automod.AutoModPhraseEntity
import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.frontend.dtos.AutoModPhraseDTO
import org.springframework.core.convert.converter.Converter


class AutoModPhraseEntityToDTOConverter : Converter<AutoModPhraseEntity, AutoModPhraseDTO> {
    override fun convert(source: AutoModPhraseEntity): AutoModPhraseDTO {
        return AutoModPhraseDTO(
            id = source.id,
            channel = source.channel!!.name,
            text = source.text,
        )
    }
}

class AutoModPhraseDTOToEntityConverter(val channelService: ChannelService) : Converter<AutoModPhraseDTO, AutoModPhraseEntity> {
    override fun convert(source: AutoModPhraseDTO): AutoModPhraseEntity {
        return AutoModPhraseEntity(
            channel = channelService.findByNameIgnoreCase(source.channel),
            text = source.text,
            id = source.id
        )
    }
}