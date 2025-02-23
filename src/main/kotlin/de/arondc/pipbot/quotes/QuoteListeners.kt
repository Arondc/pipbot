package de.arondc.pipbot.quotes

import de.arondc.pipbot.events.TwitchMessageEvent
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class QuoteListeners(
    private val quoteService: QuoteService,
) {
    @ApplicationModuleListener
    fun receiveMessage(twitchMessageEvent: TwitchMessageEvent) {
        when {
            twitchMessageEvent.messageInfo.text.startsWith("!zitat add ") -> {
                quoteService.processAdd(
                    twitchMessageEvent,
                    twitchMessageEvent.messageInfo.text.substringAfter("!zitat add "),
                    twitchMessageEvent.channel
                )
            }

            twitchMessageEvent.messageInfo.text.startsWith("!zitat delete ") -> {
                quoteService.processDelete(
                    twitchMessageEvent,
                    twitchMessageEvent.messageInfo.text.substringAfter("!zitat delete "),
                    twitchMessageEvent.channel
                )
            }

            twitchMessageEvent.messageInfo.text.startsWith("!zitat ") -> {
                quoteService.processFind(
                    twitchMessageEvent.messageInfo.text.substringAfter("!zitat ").trim(),
                    twitchMessageEvent.channel
                )
            }
        }
    }
}