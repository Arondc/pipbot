package de.arondc.pipbot.chat

import de.arondc.pipbot.streams.StreamService
import de.arondc.pipbot.twitch.TwitchMessage
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class MergeProcessor(val streamService: StreamService) {

    @ApplicationModuleListener
    fun receiveMessage(twitchMessage: TwitchMessage) {
        if(twitchMessage.message.startsWith("!merge ")){
            val args = twitchMessage.message.substringAfter("!merge ").split(" ").map { it.toLong() }.toList()
            streamService.mergeStream(args[0], args[1])
        }
    }
}