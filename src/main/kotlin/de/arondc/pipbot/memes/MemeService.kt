package de.arondc.pipbot.memes

import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.streams.TwitchStreamService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.regex.Pattern

@Service
class MemeService(val memeRepo: MemeRepository, val channelService: ChannelService, val twitchStreamService: TwitchStreamService) {
    private val log = KotlinLogging.logger {}
    fun saveMeme(channelName: String, user: String, message: String) {
        val channel = channelService.findOrCreate(channelName)
        val meme = MemeEntity(
            LocalDateTime.now(),
            channel,
            user,
            message,
            extractLink(message),
            twitchStreamService.findCurrentStream(channelName)
        )
        log.info { "Before saving: $meme" }
        val savedMeme = memeRepo.save(meme)
        log.info { "After saving: $savedMeme" }
    }

    private fun extractLink(message: String): String {
        val p = Pattern.compile("https?:(.*?)(\\s.*|$)")
        val m = p.matcher(message)
        return if (m.find()) m.group(1) else ""
    }
}



