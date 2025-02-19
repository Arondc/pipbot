package de.arondc.pipbot.users

import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.channels.ChannelRepository
import de.arondc.pipbot.events.TwitchPermission
import de.arondc.pipbot.events.UpdateChannelInformationForUserEvent
import de.arondc.pipbot.events.UpdateUserListForChannelEvent
import de.arondc.pipbot.features.Feature
import de.arondc.pipbot.features.FeatureService
import de.arondc.pipbot.streams.StreamService
import de.arondc.pipbot.twitch.TwitchException
import de.arondc.pipbot.twitch.TwitchStreamService
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class UserService(
    val userStorage: UserStorage,
    val channelRepository: ChannelRepository,
    val userChannelInformationStorage: UserChannelInformationStorage,
    val streamService: StreamService,
    val twitchStreamService: TwitchStreamService,
    val eventPublisher: ApplicationEventPublisher,
    val featureService: FeatureService,
) {
    private val log = KotlinLogging.logger {}

    fun getUserChannelInformation(userName: String, channelName: String): UserInformation? =
        userChannelInformationStorage.findByUserNameIgnoreCaseAndChannelNameIgnoreCase(userName, channelName)


    fun updateChannelInformationForUser(channelName: String, userName: String, permissions: Set<TwitchPermission> = emptySet()) {
        val user = userStorage.findByNameIgnoreCase(userName) ?: userStorage.save(UserEntity(userName))
        val channel = channelRepository.findByNameIgnoreCase(channelName)!!

        val info = userChannelInformationStorage.findByUserNameIgnoreCaseAndChannelNameIgnoreCase(userName, channelName)
            ?: userChannelInformationStorage.save(UserInformation(user,channel))

        updateHighestUserLevel(permissions, info)
        updateLastSeenOfUser(channelName, info)
        updateFollowerStatus(user, channel, info)

        userChannelInformationStorage.save(info)
    }

    private fun updateFollowerStatus(user: UserEntity, channel: ChannelEntity, info: UserInformation) {
        if (featureService.isEnabled(Feature.UpdateFollowerStatus)) {
            try {
                info.followerSince = twitchStreamService.getFollowerSince(channel.name, user.name)
                info.followerVerifiedOnce = true
            } catch (e: TwitchException) {
                log.warn(e) { "Follow age for user '${user.name}' could not be updated." }
            }
        }
    }

    private fun updateHighestUserLevel(
        permissions: Set<TwitchPermission>,
        info: UserInformation
    ) {
        if (permissions.isNotEmpty()) {
            val highestLevel = permissions.maxByOrNull { it.level } ?: TwitchPermission.EVERYONE
            info.highestTwitchUserLevel = highestLevel
        }
    }

    private fun updateLastSeenOfUser(
        channelName: String,
        info: UserInformation
    ) {
        val latestStream = streamService.findLatestStreamForChannel(channelName)
        if (latestStream != null &&
            info.lastSeen.isBefore(latestStream.startTimes.min()) ) {
            info.amountOfVisitedStreams += 1
        }
        info.lastSeen = LocalDateTime.now()
    }

    //TODO Oberfläche? (ggf. auch erst mit dem Automod)
    //TODO Doku: ER/JPA & Messaging & ggf Komponenten Diagram
    //TODO: ^----> ggf. auch restrukturieren
    //TODO Tests Tests Tests!

    fun updateUserListForChannel(channelName: String) {
        val twitchStream = twitchStreamService.fetchCurrentStreamForChannel(channelName) ?: return

        streamService.findOrCreateMatchingStream(twitchStream)
        twitchStreamService.getChatUserList(twitchStream.userLogin)
            .map { user ->
            eventPublisher.publishEvent(UpdateChannelInformationForUserEvent(twitchStream.userName, user.userName))
        }

    }
}

@Component
class UserServiceListener(val userService: UserService){
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    fun handleUpdateUserListForChannelEvent(event: UpdateUserListForChannelEvent) {
        log.debug { "received event to update user list for channel ${event.channel}" }
        userService.updateUserListForChannel(event.channel)
    }

    @ApplicationModuleListener
    fun handleUpdateChannelInformationForUserEvent(event: UpdateChannelInformationForUserEvent) {
        log.debug { "received event to update user channel information ${event.channel} - ${event.user} - ${event.permissions} " }
        userService.updateChannelInformationForUser(event.channel, event.user, event.permissions)
    }
}

