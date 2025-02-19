package de.arondc.pipbot.features

import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class FeatureService(val featureStorage: FeatureStorage) {
    private val log = KotlinLogging.logger {}

    fun disable(feature: Feature) {
        log.warn { "Deactivating feature $feature" }
        toggleFeature(feature, false)
    }

    fun enable(feature: Feature) {
        log.warn { "Activating feature $feature" }
        toggleFeature(feature, true)
    }

    fun isEnabled(feature: Feature): Boolean {
        return featureStorage.isEnabled(feature.name)
    }

    fun getFeatures(): List<FeatureEntity> {
        return featureStorage.findAll()
    }

    private fun toggleFeature(feature: Feature, enabled: Boolean) {
        val entity = featureStorage.findByName(feature.name)
            ?: throw FeatureNotFoundException("Feature ${feature.name} not found")
        entity.enabled = enabled
        featureStorage.save(entity)
    }
}

class FeatureNotFoundException(message: String) : RuntimeException(message)