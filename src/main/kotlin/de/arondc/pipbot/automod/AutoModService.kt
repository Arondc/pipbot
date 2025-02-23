package de.arondc.pipbot.automod

import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.ModerationActionEvent
import de.arondc.pipbot.events.NewAutoModPhraseEvent
import de.arondc.pipbot.events.TwitchMessageEvent.MessageInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class AutoModService(
    private val autoModPhraseRepository: AutoModPhraseRepository,
    private val eventPublisher: EventPublishingService,
    @Value("\${automod.cache_size}") private val cacheSize: Long
) {

    private final var chatMessageCache : ChatMessageCache = ChatMessageCache(cacheSize)

    fun processChat(channelName: String, userName: String, messageInfo: MessageInfo) {
        chatMessageCache.store(userName, channelName, messageInfo)
        val badPhrases = autoModPhraseRepository.findByChannelName(channel = channelName)
        val moderationDecision = decideIfModerationIsNecessary(badPhrases, messageInfo)
        if(moderationDecision) {
            eventPublisher.publishEvent(ModerationActionEvent(channelName, userName))
        }
    }

    private fun decideIfModerationIsNecessary(
        badPhrases: List<AutoModPhraseEntity>,
        messageInfo: MessageInfo
    ) = badPhrases.any { phrase ->
        messageInfo.text.contains(phrase.text,true) ||
                messageInfo.normalizedText.contains(phrase.text, true)
    }

    fun processNewPhrase(channelName: String, newPhrase: String) {
        val userChatRecords = chatMessageCache.getChatRecordsOfChannel(channelName)
        userChatRecords
            .filter { (_, messageInfos) ->
                messageInfos.any { messageInfo ->
                    messageInfo.text.contains(newPhrase, true) ||
                            messageInfo.normalizedText.contains(newPhrase, true)
                }
            }
            .map { (userName, _) -> userName }
            .forEach { user ->
                eventPublisher.publishEvent(ModerationActionEvent(channelName, user))
        }
    }

    fun findAll(): List<AutoModPhraseEntity> = autoModPhraseRepository.findAll()
    fun save(newEntity: AutoModPhraseEntity) {
        autoModPhraseRepository.save(newEntity)
        eventPublisher.publishEvent(NewAutoModPhraseEvent(newEntity.channel!!.name, newEntity.text))
    }

    fun findById(autoModPhraseId: Long) = autoModPhraseRepository.findByIdOrNull(autoModPhraseId)
    fun delete(entity: AutoModPhraseEntity) = autoModPhraseRepository.delete(entity)
}

class ChatMessageCache(private val maxCacheSize: Long){
    data class UserInChannel(val userName: String, val channelName: String)

    private val messagesOfUsers = mutableMapOf<UserInChannel, MutableList<MessageInfo>>()

    fun store(userName: String, channelName: String, messageInfo: MessageInfo) {
        messagesOfUsers.compute(UserInChannel(userName, channelName)) { _,messageList ->
            if(messageList == null) {
                mutableListOf(messageInfo)
            } else {
                messageList.add(messageInfo)
                if(messageList.size >= maxCacheSize) {
                    messageList.removeFirst()
                }
                messageList
            }
        }
    }

    fun getChatRecordsOfChannel(channel: String): Map<String, List<MessageInfo>> {
        return messagesOfUsers
            .filter { (userInChannel, _) -> userInChannel.channelName == channel }
            .map { it.key.userName to it.value }.toMap()
    }
}
