package de.arondc.pipbot.chat

import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.memes.MemeEntity
import de.arondc.pipbot.memes.MemeService
import de.arondc.pipbot.services.LanguageService
import de.arondc.pipbot.streams.StreamService
import de.arondc.pipbot.twitch.SendMessageEvent
import de.arondc.pipbot.twitch.TwitchMessage
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class MemeProcessor(
    val memeService: MemeService,
    val languageService: LanguageService,
    val channelService: ChannelService,
    val streamService: StreamService,
    val publisher: ApplicationEventPublisher
) {
    private val log = KotlinLogging.logger {}

    private val memeSources: Set<String> = setOf("imgflip.com", "www.youtube.com", "clips.twitch.tv")

    @ApplicationModuleListener
    fun receiveMessage(twitchMessage: TwitchMessage) {
        if (twitchMessage.message.startsWith("!meme ", true)) {
            processMemeMessage(twitchMessage.user, twitchMessage.channel, twitchMessage.message.substringAfter("!meme "))
            respond(twitchMessage)
        } else if (memeSources.any {
                twitchMessage.message.contains(it, true)
            }) {
            processMemeMessage(twitchMessage.user, twitchMessage.channel, twitchMessage.message)
            respond(twitchMessage)
        }
    }

    private fun respond(twitchMessage: TwitchMessage) {
        val message =
            languageService.getMessage(twitchMessage.channel, "twitch.memes.acknowledge", arrayOf(twitchMessage.user))
        publisher.publishEvent(
            SendMessageEvent(twitchMessage.channel, message)
        )
    }

    private fun processMemeMessage(channelName : String, user : String , message : String) {
        log.info { "Possible meme from $user detected: $message"}
        val channel = channelService.findOrCreate(channelName)
        val meme = MemeEntity(
            LocalDateTime.now(),
            channel,
            user,
            message,
            streamService.findCurrentStream(channelName)
        )
        log.debug { meme }
        memeService.save(meme)
    }
}