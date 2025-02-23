package de.arondc.pipbot.memes

import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchMessageEvent
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
    fun receiveMessage(twitchMessageEvent: TwitchMessageEvent) {
        if (twitchMessageEvent.messageInfo.text.startsWith("!meme ", true)) {
            handle(twitchMessageEvent, twitchMessageEvent.messageInfo.text.substringAfter("!meme "))
        } else if (memeSourceExpressions.any { regEx -> regEx.containsMatchIn(twitchMessageEvent.messageInfo.text) }) {
            handle(twitchMessageEvent, twitchMessageEvent.messageInfo.text)
        }
    }

    @ApplicationModuleListener
    fun receiveBrowserSourceMessages(twitchMessageEvent: TwitchMessageEvent) {
        if (IMGFLIP_COM.containsMatchIn(twitchMessageEvent.messageInfo.text)) {
            memeService.forwardMemeToBrowserSource(twitchMessageEvent.channel, twitchMessageEvent.messageInfo.text)
        }
    }

    private fun handle(twitchMessageEvent: TwitchMessageEvent, text: String) {
        memeService.processMemeMessage(
            twitchMessageEvent.channel, twitchMessageEvent.userInfo.userName, text
        )
        val message = languageService.getMessage(
            twitchMessageEvent.channel, "twitch.memes.acknowledge", arrayOf(twitchMessageEvent.userInfo.userName)
        )
        eventPublisher.publishEvent(SendMessageEvent(twitchMessageEvent.channel, message))

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