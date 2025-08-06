package de.arondc.pipbot.automod

import de.arondc.pipbot.events.MessageInfo
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class AutoModService(
    private val autoModPhraseRepository: AutoModPhraseRepository,
    @param:Value("\${automod.cache_size}") private val cacheSize: Long
) {

    private final var chatMessageCache : ChatMessageCache = ChatMessageCache(cacheSize)

    fun needsModeration(channelName: String, userName: String, messageInfo: MessageInfo): Boolean {
        chatMessageCache.store(userName, channelName, messageInfo)
        val badPhrases = autoModPhraseRepository.findByChannelName(channel = channelName)
        return decideIfModerationIsNecessary(badPhrases, messageInfo)
    }

    private fun decideIfModerationIsNecessary(
        badPhrases: List<AutoModPhraseEntity>,
        messageInfo: MessageInfo
    ) = badPhrases.any { phrase ->
        messageInfo.text.contains(phrase.text,true) ||
                messageInfo.normalizedText.contains(phrase.text, true)
    }

    fun processNewPhrase(channelName: String, newPhrase: String): List<String> {
        val userChatRecords = chatMessageCache.getChatRecordsOfChannel(channelName)
        return userChatRecords
            .filter { (_, messageInfos) ->
                messageInfos.any { messageInfo ->
                    messageInfo.text.contains(newPhrase, true) ||
                            messageInfo.normalizedText.contains(newPhrase, true)
                }
            }
            .map { (userName, _) -> userName }
    }

    fun findAll(): List<AutoModPhraseEntity> = autoModPhraseRepository.findAll()
    fun save(newEntity: AutoModPhraseEntity) = autoModPhraseRepository.save(newEntity)
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
