package de.arondc.pipbot.twitch

import mu.KotlinLogging
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.core.io.support.DefaultPropertySourceFactory
import org.springframework.core.io.support.EncodedResource


@Configuration
@PropertySource(value = ["file:authentication.yml"], factory = YamlPropertiesFactory::class)
@ConfigurationProperties(prefix = "twitch-connector.authentication")
data class TwitchConnectorConfig(
    var user: String = "",
    var oAuth: String = "",
    var clientSecret: String = "",
    var clientId: String = ""
)

@Configuration
@PropertySource(value = ["file:pipbot.yml"], factory = YamlPropertiesFactory::class)
@ConfigurationProperties(prefix = "twitch-connector.channels")
data class TwitchConnectorChannels(
    var channelNames : List<String> = listOf()
)

/**
 * Factory that can be used as factory in a PropertiesSource to allow loading yml files as properties
 * @see PropertySource
 */
class YamlPropertiesFactory : DefaultPropertySourceFactory() {
    private val log = KotlinLogging.logger {}
    override fun createPropertySource(
        name: String?, resource: EncodedResource
    ): org.springframework.core.env.PropertySource<*> {
        val loader = YamlPropertySourceLoader()
        val propertySource = loader.load(resource.resource.filename, resource.resource)

        log.info { "Trying to load ${resource.resource.file.absolutePath}" }

        if (propertySource.isNotEmpty()) {
            val source = propertySource.first()
            return source
        }
        return super.createPropertySource(name, resource)
    }
}