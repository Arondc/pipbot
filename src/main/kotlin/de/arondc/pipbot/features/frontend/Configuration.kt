package de.arondc.pipbot.features.frontend

import de.arondc.pipbot.features.FeatureEntity
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.format.FormatterRegistry

@Configuration
class Configuration(val formatterRegistry: FormatterRegistry) {
    @PostConstruct
    fun initConverters(){
        formatterRegistry.addConverter(FeatureEntityToDTOConverter())
        formatterRegistry.addConverter(FeatureDTOToEntityConverter())
    }
}

class FeatureEntityToDTOConverter : Converter<FeatureEntity, FeatureDTO> {
    override fun convert(source: FeatureEntity): FeatureDTO {
        return FeatureDTO(name = source.name, enabled = source.enabled)
    }
}

class FeatureDTOToEntityConverter : Converter<FeatureDTO, FeatureEntity> {
    override fun convert(source: FeatureDTO): FeatureEntity {
        return FeatureEntity(
            name = source.name,
            enabled = source.enabled,
        )
    }
}