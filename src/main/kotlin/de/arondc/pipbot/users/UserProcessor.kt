package de.arondc.pipbot.users

import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchMessageEvent
import de.arondc.pipbot.events.UpdateChannelInformationForUserEvent
import de.arondc.pipbot.services.LanguageService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class UserProcessor(
    val userService: UserService,
    val languageService: LanguageService,
    val eventPublisher: ApplicationEventPublisher,
) {
    companion object{
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss")
    }

    @ApplicationModuleListener
    fun receiveMessage(twitchMessageEvent: TwitchMessageEvent) {
        handleRequest(twitchMessageEvent.channel, twitchMessageEvent.messageInfo.text, twitchMessageEvent.userInfo.userName)

        eventPublisher.publishEvent(
            UpdateChannelInformationForUserEvent(
                twitchMessageEvent.channel,
                twitchMessageEvent.userInfo.userName,
                twitchMessageEvent.userInfo.permissions
            )
        )
    }

    private fun handleRequest(channel: String, message: String, user: String) {
        val responseMessage = when {
            message.startsWith("!lastseen") ->
                handleLastSeen(message.substringAfter("!lastseen"), channel, user)
            else -> null
        }

        responseMessage?.let {
            eventPublisher.publishEvent(SendMessageEvent(channel, responseMessage))
        }
    }

    private fun handleLastSeen(username: String, channel: String, userCalling : String) : String {
        val userParam = username.trim().ifEmpty { userCalling }

        val info = userService.getUserChannelInformation(userParam, channel)
            ?: return languageService.getMessage(
                channel,
                "users.lastseen.notFound",
                arrayOf(userParam)
            )

        return languageService.getMessage(
            channel,
            "users.lastseen.found",
            arrayOf(
                userParam,
                info.amountOfVisitedStreams,
                info.lastSeen.format(DATE_FORMAT),
                info.lastSeen.format(TIME_FORMAT)
            )
        )
    }
}

