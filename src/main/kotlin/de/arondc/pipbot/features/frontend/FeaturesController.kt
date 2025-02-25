package de.arondc.pipbot.features.frontend

import de.arondc.pipbot.features.Feature
import de.arondc.pipbot.features.FeatureService
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Controller
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Controller
@RequestMapping("/features")
class FeaturesController(val featureFrontendService: FeatureFrontendService) {

    @ModelAttribute("features")
    fun initFeaturesList(): List<FeatureDTO> {
        return featureFrontendService.getAll()
    }

    @GetMapping
    fun viewFeatures(): String {
        return "features"
    }

    @GetMapping("/disable")
    fun disableFeature(@RequestParam("feature") featureName: String): String {
        featureFrontendService.disable(featureName)
        return "redirect:/features"
    }

    @GetMapping("/enable")
    fun enableFeature(@RequestParam("feature") featureName: String): String {
        featureFrontendService.enable(featureName)
        return "redirect:/features"
    }
}

@Service
class FeatureFrontendService(val featureService: FeatureService, val conversionService: ConversionService) {

    fun getAll(): List<FeatureDTO> {
        val features = featureService.getFeatures()
        return features.mapNotNull { conversionService.convert(it, FeatureDTO::class.java) }
    }

    fun enable(featureName: String) {
        featureService.enable(Feature.valueOf(featureName))
    }

    fun disable(featureName: String) {
        featureService.disable(Feature.valueOf(featureName))
    }

}

class FeatureDTO(
    val name: String,
    val enabled: Boolean,
)



