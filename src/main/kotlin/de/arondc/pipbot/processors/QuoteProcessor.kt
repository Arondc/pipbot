package de.arondc.pipbot.processors

import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.quotes.QuoteNotFoundException
import de.arondc.pipbot.quotes.QuoteService
import de.arondc.pipbot.twitch.SendMessageEvent
import de.arondc.pipbot.twitch.TwitchMessage
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class QuoteProcessor(val quoteService: QuoteService, val channelService: ChannelService, val publisher: ApplicationEventPublisher) {
    @ApplicationModuleListener
    fun receiveMessage(twitchMessage: TwitchMessage) {
        when {
            twitchMessage.message.startsWith("!zitat add ") -> {
                processAdd(
                    twitchMessage.message.substringAfter("!zitat add "),
                    channelService.findOrCreate(twitchMessage.channel)
                )
            }
            twitchMessage.message.startsWith("!zitat delete ") -> {
                processDelete(
                    twitchMessage.message.substringAfter("!zitat delete "),
                    channelService.findOrCreate(twitchMessage.channel)
                )
            }
            twitchMessage.message.startsWith("!zitat ") -> {
                processFind(
                    twitchMessage.message.substringAfter("!zitat "),
                    channelService.findOrCreate(twitchMessage.channel)
                )
            }
        }
    }

    fun processAdd(text: String, channel: ChannelEntity) {
        //TODO schlauer machen
        val quoteNumber = quoteService.save(text, channel).number
        publisher.publishEvent(SendMessageEvent(channel.name, "Zitat #$quoteNumber wurde gespeichert"))
    }

    fun processDelete(numberAsText: String, channel: ChannelEntity) {
        val quote = quoteService.findByNumber(numberAsText.toLong(), channel)
        quoteService.delete(quote)
        publisher.publishEvent(SendMessageEvent(channel.name, "Zitat #$numberAsText wurde gelöscht"))
    }

    fun processFind(numberOrText: String, channel: ChannelEntity) {
        //TODO Format für Ausgabe schöner machen (Nummer mit ausgeben)
        try {
            val quote = when {
                numberOrText.trim().all { it.isDigit() } -> quoteService.findByNumber(
                    numberOrText.trim().toLong(),
                    channel
                )
                else -> quoteService.findByText(numberOrText, channel)
            }
            publisher.publishEvent(SendMessageEvent(channel.name, quote.text))
        } catch (qnfe : QuoteNotFoundException) {
            publisher.publishEvent(SendMessageEvent(channel.name, "Kein Zitat gefunden."))
        }
    }
}