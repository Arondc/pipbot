package de.arondc.pipbot.users

import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.channels.ChannelRepository
import de.arondc.pipbot.events.TwitchPermission
import de.arondc.pipbot.events.UpdateChannelInformationForUserEvent
import de.arondc.pipbot.events.UpdateUserListForChannelEvent
import de.arondc.pipbot.streams.StreamEntity
import de.arondc.pipbot.streams.StreamService
import de.arondc.pipbot.twitch.TwitchStreamService
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.ZoneOffset

@Service
class UserService(
    val userStorage: UserStorage,
    val channelRepository: ChannelRepository,
    val userChannelInformationStorage: UserChannelInformationStorage,
    val streamService: StreamService,
    val twitchStreamService: TwitchStreamService,
    val eventPublisher: ApplicationEventPublisher,
) {

    fun getUserChannelInformation(userName: String, channelName: String): UserChannelInformationEntity? =
        userChannelInformationStorage.findByUserNameIgnoreCaseAndChannelNameIgnoreCase(userName, channelName)


    fun updateChannelInformationForUser(channelName: String, userName: String, permissions: Set<TwitchPermission> = emptySet()) {
        fun buildNewUserChannelInformation(user: UserEntity,channel: ChannelEntity,streamIsRunning: Boolean) =
            userChannelInformationStorage.save(UserChannelInformationEntity(user,channel,
                amountOfVisitedStreams = if (streamIsRunning) 1L else 0L))

        val user = userStorage.findByNameIgnoreCase(userName) ?: userStorage.save(UserEntity(userName))
        val channel = channelRepository.findByNameIgnoreCase(channelName)!!
        val stream = streamService.findOrPersistCurrentStream(channelName)

        val info = userChannelInformationStorage.findByUserNameIgnoreCaseAndChannelNameIgnoreCase(userName, channelName)
            ?: buildNewUserChannelInformation(user, channel, stream != null)

        if (permissions.isNotEmpty()) {
            updateHighestUserLevel(permissions, info)
        }
        updateLastSeenOfUser(stream, info)
        updateFollowerStatus(user, channel, info)

        userChannelInformationStorage.save(info)
    }

    private fun updateFollowerStatus(user: UserEntity, channel: ChannelEntity, info: UserChannelInformationEntity) {
        val followInstant = twitchStreamService.getFollowerInfoFor(channel.name, user.name)
        info.followerSince =
            if (followInstant != null) LocalDateTime.ofInstant(followInstant, ZoneOffset.systemDefault()) else null
    }

    private fun updateHighestUserLevel(
        permissions: Set<TwitchPermission>,
        info: UserChannelInformationEntity
    ) {
        val highestLevel = permissions.maxByOrNull { it.level } ?: TwitchPermission.EVERYONE
        info.highestTwitchUserLevel = highestLevel
    }

    private fun updateLastSeenOfUser(
        stream: StreamEntity?,
        info: UserChannelInformationEntity
    ) {

        if (stream != null && info.lastSeen.isBefore(stream.startTimes.min())) {
            info.amountOfVisitedStreams += 1
        }
        info.lastSeen = LocalDateTime.now()
    }

    //TODO Antwort internationalisieren
    //TODO Oberfläche? (ggf. auch erst mit dem Automod)
    //TODO Doku: ER/JPA & Messaging & ggf Komponenten Diagram
    //TODO: ^----> ggf. auch restrukturieren
    //TODO Tests Tests Tests!

    fun updateUserListsForChannels(channelNames: List<String>) {
        val currentOnlineStreams = twitchStreamService.fetchCurrentStreamsForChannels(channelNames)
        currentOnlineStreams.streams.forEach { stream ->
            streamService.findOrCreateMatchingStream(stream)
            val chatterList = twitchStreamService.getChatUserList(stream.userLogin)
            chatterList.chatters.map { user ->
                eventPublisher.publishEvent(UpdateChannelInformationForUserEvent(stream.userName, user.userName))
            }
        }
    }
}

@Component
class UserServiceListener(val userService: UserService){
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    fun handleUpdateUserListForChannelEvent(event: UpdateUserListForChannelEvent) {
        log.debug { "received event to update user list for channel ${event.channel}" }
        userService.updateUserListsForChannels(listOf(event.channel))
    }

    @ApplicationModuleListener
    fun handleUpdateChannelInformationForUserEvent(event: UpdateChannelInformationForUserEvent) {
        log.debug { "received event to update user channel information ${event.channel} - ${event.user} - ${event.permissions} " }
        userService.updateChannelInformationForUser(event.channel, event.user, event.permissions)
    }
}

