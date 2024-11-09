package de.arondc.pipbot.quotes

import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchMessageEvent
import de.arondc.pipbot.events.TwitchPermission
import de.arondc.pipbot.events.satisfies
import de.arondc.pipbot.services.LanguageService
import de.arondc.pipbot.twitch.TwitchStreamService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Component
class QuoteProcessor(
    val quoteService: QuoteService,
    val channelService: ChannelService,
    val twitchStreamService: TwitchStreamService,
    val languageService: LanguageService,
    val publisher: ApplicationEventPublisher
) {
    @ApplicationModuleListener
    fun receiveMessage(twitchMessageEvent: TwitchMessageEvent) {
        when {
            twitchMessageEvent.messageInfo.text.startsWith("!zitat add ") -> {
                processAdd(
                    twitchMessageEvent,
                    twitchMessageEvent.messageInfo.text.substringAfter("!zitat add "),
                    channelService.findByNameIgnoreCase(twitchMessageEvent.channel)!!
                )
            }

            twitchMessageEvent.messageInfo.text.startsWith("!zitat delete ") -> {
                processDelete(
                    twitchMessageEvent,
                    twitchMessageEvent.messageInfo.text.substringAfter("!zitat delete "),
                    channelService.findByNameIgnoreCase(twitchMessageEvent.channel)!!
                )
            }

            twitchMessageEvent.messageInfo.text.startsWith("!zitat ") -> {
                processFind(
                    twitchMessageEvent.messageInfo.text.substringAfter("!zitat "),
                    channelService.findByNameIgnoreCase(twitchMessageEvent.channel)!!
                )
            }
        }
    }

    fun processAdd(twitchMessageEvent: TwitchMessageEvent, text: String, channel: ChannelEntity) {
        if (twitchMessageEvent.userInfo.permissions.satisfies(TwitchPermission.SUBSCRIBER)) {
            val date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            val game = twitchStreamService.findLastGameFor(channel.name)
            val quoteNumber = quoteService.save("$text ($game - $date)", channel).number
            val message = languageService.getMessage(channel.name, "quote.added", arrayOf(quoteNumber))
            publisher.publishEvent(SendMessageEvent(channel.name, message))
        }
    }

    fun processDelete(twitchMessageEvent: TwitchMessageEvent, numberAsText: String, channel: ChannelEntity) {
        if (twitchMessageEvent.userInfo.permissions.satisfies(TwitchPermission.MODERATOR)) {
            val quote = quoteService.findByNumber(numberAsText.toLong(), channel)
            quoteService.delete(quote)
            val message = languageService.getMessage(channel.name, "quote.deleted", arrayOf(numberAsText))
            publisher.publishEvent(SendMessageEvent(channel.name, message))
        }
    }

    fun processFind(numberOrText: String, channel: ChannelEntity) {
        try {
            val quote = when {
                numberOrText.trim().all { it.isDigit() } -> quoteService.findByNumber(
                    numberOrText.trim().toLong(),
                    channel
                )

                else -> quoteService.findByText(numberOrText, channel)
            }
            publisher.publishEvent(SendMessageEvent(channel.name, "#${quote.number} ${quote.text}"))
        } catch (qnfe: QuoteNotFoundException) {
            val message = languageService.getMessage(channel.name, "quote.error.does_not_exist")
            publisher.publishEvent(SendMessageEvent(channel.name, message))
        }
    }
}