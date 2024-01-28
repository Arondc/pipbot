package de.arondc.pipbot

import de.arondc.pipbot.twitchConnector.TwitchConnector
import mu.KotlinLogging
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.boot.runApplication
import org.springframework.context.annotation.PropertySource
import org.springframework.core.io.support.DefaultPropertySourceFactory
import org.springframework.core.io.support.EncodedResource

fun main(args: Array<String>) {
    runApplication<PipbotV2Application>(*args)
}

@SpringBootApplication
class PipbotV2Application(val twitchConnector: TwitchConnector) : CommandLineRunner {

    override fun run(vararg args: String?) {
        twitchConnector.start()
    }
}

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