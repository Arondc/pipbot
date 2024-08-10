package de.arondc.pipbot.users

import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchMessage
import de.arondc.pipbot.events.TwitchPermission
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component
import java.time.format.DateTimeFormatter

@Component
class UserProcessor(
    val userService: UserService,
    val applicationEventPublisher: ApplicationEventPublisher,
) {
    companion object{
        private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        private val TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss")
    }

    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    fun receiveMessage(twitchMessage: TwitchMessage) {
        recognizeUser(twitchMessage.channel, twitchMessage.user, twitchMessage.permissions)
        handleRequest(twitchMessage.channel, twitchMessage.message, twitchMessage.user)
    }

    private fun recognizeUser(channel: String, userName: String, permissions: Set<TwitchPermission>) {
        userService.recognizeUser(channel, userName, permissions)
    }

    private fun handleRequest(channel: String, message: String, user: String) {

        val responseMessage = when {
            message.startsWith("!lastseen") -> handleLastSeen(message.substringAfter("!lastseen"), channel, user)
            else -> null
        }

        responseMessage?.let {
            applicationEventPublisher.publishEvent(SendMessageEvent(channel, responseMessage))
        }
    }

    private fun handleLastSeen(username: String, channel: String, userCalling : String) : String {
        val userParam = username.trim().ifEmpty { userCalling }
        val info = userService.getUserChannelInformation(userParam, channel) ?: return "Ich kenne $userParam nicht!"
        return "$userParam war schon ${info.amountOfVisitedStreams}x da, zuletzt am ${info.lastSeen?.format(DATE_FORMAT)} um ${
            info.lastSeen?.format(
                TIME_FORMAT
            )
        }"
    }
}

