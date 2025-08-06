package de.arondc.pipbot.memes

import de.arondc.pipbot.events.CallType
import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.ProcessingEvent
import de.arondc.pipbot.events.TwitchCallEvent
import de.arondc.pipbot.services.LanguageService
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class MemeListeners(
    private val memeService: MemeService,
    private val languageService: LanguageService,
    private val eventPublisher: EventPublishingService
) {

    @ApplicationModuleListener
    fun receiveMessage(processingEvent: ProcessingEvent) {
        if (processingEvent.messageInfo.text.startsWith("!meme ", true)) {
            handle(processingEvent, processingEvent.messageInfo.text.substringAfter("!meme "))
        } else if (memeSourceExpressions.any { regEx -> regEx.containsMatchIn(processingEvent.messageInfo.text) }) {
            handle(processingEvent, processingEvent.messageInfo.text)
        }
    }

    @ApplicationModuleListener
    fun receiveBrowserSourceMessages(processingEvent: ProcessingEvent) {
        if (IMGFLIP_COM.containsMatchIn(processingEvent.messageInfo.text)) {
            memeService.forwardMemeToBrowserSource(processingEvent.channel, processingEvent.messageInfo.text)
        }
    }

    private fun handle(processingEvent: ProcessingEvent, text: String) {
        memeService.processMemeMessage(
            processingEvent.channel, processingEvent.userInfo.userName, text
        )
        val message = languageService.getMessage(
            processingEvent.channel, "twitch.memes.acknowledge", arrayOf(processingEvent.userInfo.userName)
        )
        eventPublisher.publishEvent(TwitchCallEvent(CallType.SEND_MESSAGE, processingEvent.channel, message))

    }

    companion object {
        private val IMGFLIP_COM = "imgflip.com".toRegex(RegexOption.IGNORE_CASE)

        private val memeSourceExpressions: Set<Regex> = setOf(
            IMGFLIP_COM,
            "www.youtube.com".toRegex(RegexOption.IGNORE_CASE),
            "clips.twitch.tv".toRegex(RegexOption.IGNORE_CASE),
            "www.twitch.tv/.*/clip".toRegex(RegexOption.IGNORE_CASE)
        )
    }
}