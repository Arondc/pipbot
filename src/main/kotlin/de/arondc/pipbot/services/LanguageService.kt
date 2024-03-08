package de.arondc.pipbot.services

import de.arondc.pipbot.channels.ChannelService
import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource
import org.springframework.stereotype.Service
import java.util.Locale

@Configuration
class MessageConfig {
    @Bean("messageSource")
    fun getMessageSource(): MessageSource {
        val resourceBundleMessageSource = ReloadableResourceBundleMessageSource()
        resourceBundleMessageSource.setBasename("classpath:messages")
        resourceBundleMessageSource.setFallbackToSystemLocale(false)
        return resourceBundleMessageSource
    }
}

@Service
class LanguageService(val channelService: ChannelService, val messageSource: MessageSource) {
    fun getMessage(channelName: String, messageKey: String, args: Array<Any>? = null): String {
        val locale = channelService.findByNameIgnoreCase(channelName)?.language ?: Locale.getDefault()
        return messageSource.getMessage(messageKey, args, locale)
    }

    @JvmOverloads
    fun getMessage(messageKey: String, args: Array<Any>? = null) : String {
        val locale = Locale.getDefault()
        return messageSource.getMessage(messageKey, args, locale)
    }
}

