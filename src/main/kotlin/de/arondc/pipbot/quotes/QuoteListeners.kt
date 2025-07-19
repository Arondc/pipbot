package de.arondc.pipbot.quotes

import de.arondc.pipbot.events.ProcessingEvent
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class QuoteListeners(
    private val quoteService: QuoteService,
) {
    @ApplicationModuleListener
    fun receiveMessage(processingEvent: ProcessingEvent) {
        when {
            processingEvent.messageInfo.text.startsWith("!zitat add ") -> {
                quoteService.processAdd(
                    processingEvent,
                    processingEvent.messageInfo.text.substringAfter("!zitat add "),
                )
            }

            processingEvent.messageInfo.text.startsWith("!zitat delete ") -> {
                quoteService.processDelete(
                    processingEvent,
                    processingEvent.messageInfo.text.substringAfter("!zitat delete "),
                )
            }

            processingEvent.messageInfo.text.startsWith("!zitat ") -> {
                quoteService.processFind(
                    processingEvent.messageInfo.text.substringAfter("!zitat ").trim(),
                    processingEvent.channel
                )
            }
        }
    }
}