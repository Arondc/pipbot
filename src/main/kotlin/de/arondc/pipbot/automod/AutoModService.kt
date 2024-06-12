package de.arondc.pipbot.automod

import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.events.NewAutoModPhraseEvent
import de.arondc.pipbot.events.SendMessageEvent
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AutoModService(
    val applicationEventPublisher: ApplicationEventPublisher,
    val autoModPhraseRepository: AutoModPhraseRepository,
    @Value("\${automod.cache_size}") private val cacheSize: Long
) {
    private val log = KotlinLogging.logger {}
    private final var chatMessageCache : ChatMessageCache = ChatMessageCache(cacheSize)

    fun processChat(channel: ChannelEntity, userName: String, message: String) {
        chatMessageCache.store(userName, channel.name, message)
        val badPhrases = autoModPhraseRepository.findByChannelName(channel = channel.name)
        if(badPhrases.any { phrase -> message.contains(phrase.text, true) }) {
            applicationEventPublisher.publishEvent(
                SendMessageEvent(
                    channel = channel.name,
                    message = "$userName war böse"
                )
            )
        }
    }

    fun processNewPhrase(channel: ChannelEntity, newPhrase: String) {
        val actionableAccounts = chatMessageCache.check(channel.name, newPhrase)
        actionableAccounts.forEach {
            applicationEventPublisher.publishEvent(
                SendMessageEvent(
                    channel = channel.name,
                    message = "$it war böse"
                )
            )
        }
    }

    fun findAll(): List<AutoModPhraseEntity> = autoModPhraseRepository.findAll()
    @Transactional
    fun save(newEntity: AutoModPhraseEntity) {
        autoModPhraseRepository.save(newEntity)
        applicationEventPublisher.publishEvent(NewAutoModPhraseEvent(newEntity.channel!!.name, newEntity.text))
    }

    fun findById(autoModPhraseId: Long) = autoModPhraseRepository.findByIdOrNull(autoModPhraseId)
    fun delete(entity: AutoModPhraseEntity) = autoModPhraseRepository.delete(entity)
}

class ChatMessageCache(private val cacheSize: Long){
    data class UserInChannel(val userName: String, val channelName: String)

    private val messagesOfUsers = mutableMapOf<UserInChannel, MutableList<String>>()

    fun store(userName: String, channelName: String, message: String) {
        messagesOfUsers.compute(UserInChannel(userName, channelName)) { _,messageList ->
            if(messageList == null) {
                mutableListOf(message)
            } else {
                messageList.add(message)
                if(cacheSize > 3) {
                    messageList.removeFirst()
                }
                messageList
            }
        }
    }

    //Prüfe welche Benutzer für einen Kanal einen Treffer mit einer Phrase Y haben
    fun check(channel:String, phrase: String) =
        messagesOfUsers.filter { (userInChannel,_) -> userInChannel.channelName == channel  }
            .filter { (_,messages) -> messages.any { message -> message.contains(phrase, true) }  }
            .map { it.key.userName }

}
