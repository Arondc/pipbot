package de.arondc.pipbot.memes

import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchMessageEvent
import de.arondc.pipbot.services.LanguageService
import de.arondc.pipbot.streams.StreamService
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class MemeProcessor(
    private val memeService: MemeService,
    private val languageService: LanguageService,
    private val channelService: ChannelService,
    private val streamService: StreamService,
    private val publisher: ApplicationEventPublisher
) {
    private val log = KotlinLogging.logger {}

    private val IMGFLIP_COM = "imgflip.com".toRegex(RegexOption.IGNORE_CASE)

    private  val memeSourceExpressions : Set<Regex> = setOf(
        IMGFLIP_COM,
        "www.youtube.com".toRegex(RegexOption.IGNORE_CASE),
        "clips.twitch.tv".toRegex(RegexOption.IGNORE_CASE),
        "www.twitch.tv/.*/clip".toRegex(RegexOption.IGNORE_CASE)
    )

    @ApplicationModuleListener
    fun receiveMessage(twitchMessageEvent: TwitchMessageEvent) {
        if (twitchMessageEvent.messageInfo.text.startsWith("!meme ", true)) {
            processMemeMessage(
                twitchMessageEvent.channel,
                twitchMessageEvent.userInfo.userName,
                twitchMessageEvent.messageInfo.text.substringAfter("!meme ")
            )
            respond(twitchMessageEvent)
        } else if (memeSourceExpressions.any { regEx -> regEx.containsMatchIn(twitchMessageEvent.messageInfo.text)})
        {
            processMemeMessage(twitchMessageEvent.channel, twitchMessageEvent.userInfo.userName, twitchMessageEvent.messageInfo.text)
            respond(twitchMessageEvent)
        }
    }

    @ApplicationModuleListener
    fun receiveBrowserSourceMessages(twitchMessageEvent: TwitchMessageEvent) {
        if(IMGFLIP_COM.containsMatchIn(twitchMessageEvent.messageInfo.text)){
            memeService.forwardMemeToBrowserSource(twitchMessageEvent.channel, twitchMessageEvent.messageInfo.text)
        }
    }

    private fun respond(twitchMessageEvent: TwitchMessageEvent) {
        val message =
            languageService.getMessage(twitchMessageEvent.channel, "twitch.memes.acknowledge", arrayOf(twitchMessageEvent.userInfo))
        publisher.publishEvent(
            SendMessageEvent(twitchMessageEvent.channel, message)
        )
    }

    private fun processMemeMessage(channelName: String, user: String, message: String) {
        log.info { "Possible meme from $user detected: $message" }
        val channel = channelService.findByNameIgnoreCase(channelName)
        val meme = MemeEntity(
            LocalDateTime.now(),
            channel,
            user,
            message,
            streamService.findOrPersistCurrentStream(channelName)
        )
        log.debug { meme }
        memeService.save(meme)
    }
}