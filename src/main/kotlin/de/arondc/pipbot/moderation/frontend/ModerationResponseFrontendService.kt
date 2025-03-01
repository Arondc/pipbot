package de.arondc.pipbot.moderation.frontend

import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.frontend.FrontendException
import de.arondc.pipbot.moderation.ModerationResponeType
import de.arondc.pipbot.moderation.ModerationResponseEntity
import de.arondc.pipbot.moderation.ModerationResponseStorage
import de.arondc.pipbot.moderation.UserTrustLevel
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.converter.Converter
import org.springframework.data.repository.findByIdOrNull
import org.springframework.format.FormatterRegistry
import org.springframework.stereotype.Service
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Service
class ModerationResponseFrontendService(
    private val moderationResponseStorage: ModerationResponseStorage,
    private val channelService: ChannelService,
    private val conversionService: ConversionService
) {
    fun getModerationResponses(): List<ModerationResponseDTO> {
        return moderationResponseStorage.findAll()
            .mapNotNull { conversionService.convert(it, ModerationResponseDTO::class.java) }.toList()
    }

    fun createModerationResponse(moderationResponseDTO: ModerationResponseDTO) {
        val newModerationResponse =
            conversionService.convert(moderationResponseDTO, ModerationResponseEntity::class.java)!!
        val existingModerationResponse = moderationResponseStorage.findByChannelAndTrustLevel(
            newModerationResponse.channel, newModerationResponse.trustLevel
        )
        if (existingModerationResponse != null) {
            throw FrontendException("There already exists a moderation response configuration for ${newModerationResponse.channel.name} and trustLevel ${newModerationResponse.trustLevel}")
        }
        moderationResponseStorage.save(newModerationResponse)
    }

    fun getChannels(): List<ModerationChannelDTO> {
        return channelService.findAll().map { ModerationChannelDTO(name = it.name) }.toList()
    }

    fun deleteModerationResponse(moderationResponseId: Long) {
        if (moderationResponseStorage.findByIdOrNull(moderationResponseId) == null) {
            throw FrontendException("There is no moderation response with id $moderationResponseId")
        }
        moderationResponseStorage.deleteById(moderationResponseId)
    }
}

class ModerationResponseEntityToDTOConverter : Converter<ModerationResponseEntity, ModerationResponseDTO> {
    override fun convert(source: ModerationResponseEntity): ModerationResponseDTO {
        return ModerationResponseDTO(
            id = source.id,
            channel = source.channel.name,
            text = source.text ?: "",
            trustLevel = source.trustLevel.name,
            type = source.type.name,
            duration = source.duration?.toString() ?: ""
        )
    }
}

class ModerationResponseDTOToEntityConverter(private val channelService: ChannelService) :
    Converter<ModerationResponseDTO, ModerationResponseEntity> {
    override fun convert(source: ModerationResponseDTO): ModerationResponseEntity {
        return ModerationResponseEntity(
            channel = channelService.findByNameIgnoreCase(source.channel),
            text = source.text,
            trustLevel = UserTrustLevel.valueOf(source.trustLevel),
            type = ModerationResponeType.valueOf(source.type),
            duration = if (source.duration.isBlank()) null else source.duration.toLong(),
            id = source.id
        )
    }
}

@Configuration
class ModerationResponseWebConfig(private val channelService: ChannelService) : WebMvcConfigurer {
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(ModerationResponseEntityToDTOConverter())
        registry.addConverter(ModerationResponseDTOToEntityConverter(channelService))
    }
}
