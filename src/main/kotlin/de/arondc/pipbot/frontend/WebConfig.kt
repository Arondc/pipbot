package de.arondc.pipbot.frontend

import de.arondc.pipbot.frontend.converters.ChannelDTOToEntityConverter
import de.arondc.pipbot.frontend.converters.ChannelEntityToDTOConverter
import de.arondc.pipbot.frontend.converters.MemeEntityToDTOConverter
import org.springframework.context.annotation.Configuration
import org.springframework.format.FormatterRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverter(MemeEntityToDTOConverter())
        registry.addConverter(ChannelEntityToDTOConverter())
        registry.addConverter(ChannelDTOToEntityConverter())
    }
}

