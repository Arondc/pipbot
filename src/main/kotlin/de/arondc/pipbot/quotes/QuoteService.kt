package de.arondc.pipbot.quotes

import de.arondc.pipbot.channels.ChannelEntity
import org.springframework.stereotype.Service

@Service
class QuoteService(val quoteRepository: QuoteRepository) {

    fun save(text: String, channelEntity: ChannelEntity): QuoteEntity {
        val newNumber = 1 + (quoteRepository.findMaxNumberByChannel(channelEntity) ?: 0)
        val quote = QuoteEntity(text, newNumber, channelEntity)
        return quoteRepository.save(quote)
    }

    fun findByNumber(number: Long, channelEntity: ChannelEntity): QuoteEntity =
        quoteRepository.findByNumberAndChannel(number, channelEntity) ?: throw QuoteNotFoundException()

    fun findByText(text: String, channelEntity: ChannelEntity): QuoteEntity {
        try {
            return quoteRepository.findByTextContainingIgnoreCaseAndChannel(text, channelEntity).random()
        } catch (e: NoSuchElementException) {
            throw QuoteNotFoundException()
        }
    }


    fun delete(quoteEntity: QuoteEntity) {
        quoteRepository.delete(quoteEntity)
    }
}

class QuoteNotFoundException : RuntimeException()