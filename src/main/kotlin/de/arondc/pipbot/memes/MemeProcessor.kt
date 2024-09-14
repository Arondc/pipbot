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

private const val IMGFLIP_COM = "imgflip.com"

@Component
class MemeProcessor(
    val memeService: MemeService,
    val languageService: LanguageService,
    val channelService: ChannelService,
    val streamService: StreamService,
    val publisher: ApplicationEventPublisher
) {
    private val log = KotlinLogging.logger {}

    private val memeSources: Set<String> = setOf(IMGFLIP_COM, "www.youtube.com", "clips.twitch.tv")

    @ApplicationModuleListener
    fun receiveMessage(twitchMessageEvent: TwitchMessageEvent) {
        if (twitchMessageEvent.messageInfo.text.startsWith("!meme ", true)) {
            processMemeMessage(
                twitchMessageEvent.channel,
                twitchMessageEvent.userInfo.userName,
                twitchMessageEvent.messageInfo.text.substringAfter("!meme ")
            )
            respond(twitchMessageEvent)
        } else if (memeSources.any {
                twitchMessageEvent.messageInfo.text.contains(it, true)
            }) {
            processMemeMessage(twitchMessageEvent.channel, twitchMessageEvent.userInfo.userName, twitchMessageEvent.messageInfo.text)
            respond(twitchMessageEvent)
        }
    }

    @ApplicationModuleListener
    fun receiveBrowserSourceMessages(twitchMessageEvent: TwitchMessageEvent) {
        if(twitchMessageEvent.messageInfo.text.contains(IMGFLIP_COM, true)){
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