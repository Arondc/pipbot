package de.arondc.pipbot.memes

import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchMessage
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
    fun receiveMessage(twitchMessage: TwitchMessage) {
        if (twitchMessage.message.startsWith("!meme ", true)) {
            processMemeMessage(
                twitchMessage.channel,
                twitchMessage.user,
                twitchMessage.message.substringAfter("!meme ")
            )
            respond(twitchMessage)
        } else if (memeSources.any {
                twitchMessage.message.contains(it, true)
            }) {
            processMemeMessage(twitchMessage.channel, twitchMessage.user, twitchMessage.message)
            respond(twitchMessage)
        }
    }

    @ApplicationModuleListener
    fun receiveBrowserSourceMessages(twitchMessage: TwitchMessage) {
        if(twitchMessage.message.contains(IMGFLIP_COM, true)){
            memeService.forwardMemeToBrowserSource(twitchMessage.channel, twitchMessage.message)
        }
    }

    private fun respond(twitchMessage: TwitchMessage) {
        val message =
            languageService.getMessage(twitchMessage.channel, "twitch.memes.acknowledge", arrayOf(twitchMessage.user))
        publisher.publishEvent(
            SendMessageEvent(twitchMessage.channel, message)
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