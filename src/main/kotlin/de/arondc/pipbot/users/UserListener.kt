package de.arondc.pipbot.users

import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.TwitchMessageEvent
import de.arondc.pipbot.events.UpdateChannelInformationForUserEvent
import de.arondc.pipbot.events.UpdateUserListForChannelEvent
import mu.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class UserListener(
    val userService: UserService,
    val eventPublisher: EventPublishingService,
) {
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    fun receiveMessage(twitchMessageEvent: TwitchMessageEvent) {
        val message = twitchMessageEvent.messageInfo.text
        if (message.startsWith("!lastseen")) {
            val userName = message.substringAfter("!lastseen").trim()
                .ifEmpty { twitchMessageEvent.userInfo.userName }
            userService.handleLastSeen(userName, twitchMessageEvent.channel)
        }

        eventPublisher.publishEvent(
            UpdateChannelInformationForUserEvent(
                twitchMessageEvent.channel,
                twitchMessageEvent.userInfo.userName,
                twitchMessageEvent.userInfo.permissions
            )
        )
    }

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
