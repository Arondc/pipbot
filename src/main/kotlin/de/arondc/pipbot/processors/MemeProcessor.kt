package de.arondc.pipbot.processors

import de.arondc.pipbot.memes.MemeService
import de.arondc.pipbot.twitch.SendMessageEvent
import de.arondc.pipbot.twitch.TwitchMessage
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component
import java.text.MessageFormat

@Component
class MemeProcessor(val memeService : MemeService, val publisher: ApplicationEventPublisher) {
    private val log = KotlinLogging.logger {}

    private val memeSources: Set<String> = setOf("imgflip.com", "www.youtube.com", "clips.twitch.tv")

    @ApplicationModuleListener
    fun receiveMessage(twitchMessage: TwitchMessage) {
        if (twitchMessage.message.startsWith("!meme ", true) || memeSources.any {
                twitchMessage.message.contains(it, true)
            }) {
            processMemeMessage(twitchMessage)
            respond(twitchMessage)
        }
    }

    private fun respond(twitchMessage: TwitchMessage) {
        publisher.publishEvent(
            SendMessageEvent(twitchMessage.channel, "Danke für das Meme ${twitchMessage.user}!")
        )
    }

    private fun processMemeMessage(twitchMessage: TwitchMessage) {
        log.info { MessageFormat.format("Possible meme from {0} detected: {1}",twitchMessage.user , twitchMessage.message) }
        memeService.saveMeme(twitchMessage.channel, twitchMessage.user, twitchMessage.message)

    }
}