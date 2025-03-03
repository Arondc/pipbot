package de.arondc.pipbot.frontend

import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.frontend.converters.*
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(private val channelService: ChannelService) : WebMvcConfigurer {
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(MemeEntityToDTOConverter())
        registry.addConverter(ChannelEntityToDTOConverter())
        registry.addConverter(ChannelDTOToEntityConverter())
        registry.addConverter(StreamDTOToEntityConverter(channelService))
        registry.addConverter(StreamEntityToDTOConverter())
        registry.addConverter(AutoResponseDTOToEntityConverter(channelService))
        registry.addConverter(AutoResponseEntityToDTOConverter())
        registry.addConverter(AutoModPhraseDTOToEntityConverter(channelService))
        registry.addConverter(AutoModPhraseEntityToDTOConverter())
        registry.addConverter(ChannelEntityToAutoModChannelDTOConverter())
    }
}

