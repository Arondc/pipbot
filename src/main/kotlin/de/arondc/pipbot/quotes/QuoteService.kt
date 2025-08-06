package de.arondc.pipbot.quotes

import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.events.*
import de.arondc.pipbot.services.LanguageService
import de.arondc.pipbot.twitch.TwitchStreamService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Service
class QuoteService(
    private val quoteRepository: QuoteRepository,
    private val channelService: ChannelService,
    private val twitchStreamService: TwitchStreamService,
    private val languageService: LanguageService,
    private val eventPublisher: EventPublishingService
) {

    private fun save(text: String, channelEntity: ChannelEntity): QuoteEntity {
        val newNumber = 1 + (quoteRepository.findMaxNumberByChannel(channelEntity) ?: 0)
        val quote = QuoteEntity(text, newNumber, channelEntity)
        return quoteRepository.save(quote)
    }

    private fun findByNumber(number: Long, channelEntity: ChannelEntity): QuoteEntity =
        quoteRepository.findByNumberAndChannel(number, channelEntity) ?: throw QuoteNotFoundException()

    private fun findByText(text: String, channelEntity: ChannelEntity): List<QuoteEntity> {
        try {
            return quoteRepository.findByTextContainingIgnoreCaseAndChannel(text, channelEntity)
        } catch (_: NoSuchElementException) {
            throw QuoteNotFoundException()
        }
    }


    fun processAdd(processingEvent: ProcessingEvent, text: String) {
        if (processingEvent.userInfo.permissions.satisfies(TwitchPermission.SUBSCRIBER)) {
            val channel = channelService.findByNameIgnoreCase(processingEvent.channel)
            val date = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            val game = twitchStreamService.findLastGameFor(channel.name)
            val quoteNumber = save("$text ($game - $date)", channel).number
            val message = languageService.getMessage(channel.name, "quote.added", arrayOf(quoteNumber))
            eventPublisher.publishEvent(TwitchCallEvent(CallType.SEND_MESSAGE, channel.name, message))
        }
    }

    fun processDelete(processingEvent: ProcessingEvent, numberAsText: String) {

        if (processingEvent.userInfo.permissions.satisfies(TwitchPermission.MODERATOR)) {
            val channel = channelService.findByNameIgnoreCase(processingEvent.channel)
            val quote = findByNumber(numberAsText.toLong(), channel)
            quoteRepository.delete(quote)
            val message = languageService.getMessage(channel.name, "quote.deleted", arrayOf(numberAsText))
            eventPublisher.publishEvent(TwitchCallEvent(CallType.SEND_MESSAGE, channel.name, message))
        }
    }

    fun processFind(numberOrText: String, channelName: String) {
        val channel = channelService.findByNameIgnoreCase(channelName)
        try {
            val quote = when {
                numberOrText.all { it.isDigit() } -> findByNumber(numberOrText.toLong(), channel)
                else -> findByText(numberOrText, channel).random()
            }
            eventPublisher.publishEvent(TwitchCallEvent(CallType.SEND_MESSAGE, channel.name, "#${quote.number} ${quote.text}"))
        } catch (_ : QuoteNotFoundException) {
            val message = languageService.getMessage(channel.name, "quote.error.does_not_exist")
            eventPublisher.publishEvent(TwitchCallEvent(CallType.SEND_MESSAGE, channel.name, message))
        }
    }
}

class QuoteNotFoundException : RuntimeException()