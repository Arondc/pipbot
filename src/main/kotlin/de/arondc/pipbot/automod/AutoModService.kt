package de.arondc.pipbot.automod

import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.events.ModerationActionEvent
import de.arondc.pipbot.events.NewAutoModPhraseEvent
import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchPermission
import de.arondc.pipbot.users.UserService
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class AutoModService(
    val autoModPhraseRepository: AutoModPhraseRepository,
    val userService: UserService,
    val eventPublisher: ApplicationEventPublisher,
    @Value("\${automod.cache_size}") private val cacheSize: Long
) {
    private val log = KotlinLogging.logger {}
    private final var chatMessageCache : ChatMessageCache = ChatMessageCache(cacheSize)

    @Transactional
    fun processChat(channel: ChannelEntity, userName: String, message: String) {
        chatMessageCache.store(userName, channel.name, message)

        val badPhrases = autoModPhraseRepository.findByChannelName(channel = channel.name)
        val moderationDecision = badPhrases.any { phrase -> message.contains(phrase.text, true) }
        if(moderationDecision) {
            eventPublisher.publishEvent(ModerationActionEvent(channel.name, userName))
        }
    }

    @Transactional
    fun processNewPhrase(channel: ChannelEntity, newPhrase: String) {
        val userChatRecords = chatMessageCache.getChatRecordsOfChannel(channel.name)
        userChatRecords
            .filter { (_, messages) ->
                messages.any { message -> message.contains(newPhrase, true) }
            }
            .map { (userName, _) -> userName }
            .forEach { user ->
                eventPublisher.publishEvent(ModerationActionEvent(channel.name, user))
        }
    }

    fun findAll(): List<AutoModPhraseEntity> = autoModPhraseRepository.findAll()
    @Transactional
    fun save(newEntity: AutoModPhraseEntity) {
        autoModPhraseRepository.save(newEntity)
        eventPublisher.publishEvent(NewAutoModPhraseEvent(newEntity.channel!!.name, newEntity.text))
    }

    fun findById(autoModPhraseId: Long) = autoModPhraseRepository.findByIdOrNull(autoModPhraseId)
    fun delete(entity: AutoModPhraseEntity) = autoModPhraseRepository.delete(entity)

    @ApplicationModuleListener
    fun processModerationActionEvent(event: ModerationActionEvent) {
        val userInformation = userService.getUserChannelInformation(event.user, event.channel)
            ?: throw RuntimeException("Nutzer unbekannt")

        if (TwitchPermission.MODERATOR.isSatisfiedBy(userInformation.highestTwitchUserLevel)) {
            return
        }

        when {
            userInformation.followerSince != null && ChronoUnit.MONTHS.between(
                userInformation.followerSince, LocalDateTime.now()
            ) >= 6 -> {
                eventPublisher.publishEvent(
                    SendMessageEvent(
                        channel = event.channel, message = "${event.user} war böse"
                    )
                )
            }

            else -> {
                eventPublisher.publishEvent(
                    SendMessageEvent(
                        channel = event.channel, message = "${event.user} war SEHR böse"
                    )
                )
            }
        }
    }

}

class ChatMessageCache(private val maxCacheSize: Long){
    data class UserInChannel(val userName: String, val channelName: String)

    private val messagesOfUsers = mutableMapOf<UserInChannel, MutableList<String>>()

    fun store(userName: String, channelName: String, message: String) {
        messagesOfUsers.compute(UserInChannel(userName, channelName)) { _,messageList ->
            if(messageList == null) {
                mutableListOf(message)
            } else {
                messageList.add(message)
                if(messageList.size >= maxCacheSize) {
                    messageList.removeFirst()
                }
                messageList
            }
        }
    }

    fun getChatRecordsOfChannel(channel: String): Map<String, List<String>> {
        return messagesOfUsers
            .filter { (userInChannel, _) -> userInChannel.channelName == channel }
            .map { it.key.userName to it.value }.toMap()
    }
}
