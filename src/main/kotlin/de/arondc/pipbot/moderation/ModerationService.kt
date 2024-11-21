package de.arondc.pipbot.moderation

import de.arondc.pipbot.core.Functions.retry
import de.arondc.pipbot.events.ModerationActionEvent
import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchPermission
import de.arondc.pipbot.users.UserInformation
import de.arondc.pipbot.users.UserService
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Service
class ModerationService(
    private val userService: UserService,
    private val moderationResponseStorage: ModerationResponseStorage,
    private val eventPublisher: ApplicationEventPublisher
) {
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    fun processModerationActionEvent(event: ModerationActionEvent) {
        val userInformation = retry(RuntimeException("Nutzer unbekannt")) {
            userService.getUserChannelInformation(
                event.user,
                event.channel
            )
        }

        if (TwitchPermission.MODERATOR.isSatisfiedBy(userInformation.highestTwitchUserLevel)) {
            return
        }

        val trustLevel = getUserTrustLevel(userInformation)

        val moderationResponseConfiguration =
            moderationResponseStorage.findByChannelAndTrustLevel(userInformation.channel, trustLevel)
                ?: return

        val moderationResponse = when (moderationResponseConfiguration.type) {
            ModerationResponeType.BAN -> ModerationResponse.Ban(event.user)
            ModerationResponeType.TIMEOUT -> ModerationResponse.Timeout(event.user, moderationResponseConfiguration.duration!!)
            ModerationResponeType.TEXT -> ModerationResponse.Text(event.user,  moderationResponseConfiguration.text!!)
        }

        log.info { "Actioning on Moderation event for ${event.user} with trust level $trustLevel in ${event.channel}" }
        eventPublisher.publishEvent(
            SendMessageEvent(
                channel = event.channel, message = moderationResponse.message()
            )
        )
    }

    fun getUserTrustLevel(userInformation: UserInformation) : UserTrustLevel {
        fun isFollower():Boolean = userInformation.followerSince != null
        fun followAgeInMonths(): Long = ChronoUnit.MONTHS.between(userInformation.followerSince, LocalDateTime.now())
        return when {
            isFollower() && followAgeInMonths() < 6 -> UserTrustLevel.NEW_FOLLOWER
            isFollower() && followAgeInMonths() < 12 -> UserTrustLevel.SHORT_TERM_FOLLOWER
            isFollower() && followAgeInMonths() >= 12 -> UserTrustLevel.LONG_TERM_FOLLOWER
            else -> UserTrustLevel.VIEWER
        }
    }
}

enum class UserTrustLevel{
    VIEWER,NEW_FOLLOWER,SHORT_TERM_FOLLOWER,LONG_TERM_FOLLOWER
}

sealed class ModerationResponse(val userName: String = ""){
    class Ban(userName: String): ModerationResponse(userName) {
        override  fun message(): String = "/ban $userName"
    }
    class Timeout(userName: String, private val timeoutDuration:Long): ModerationResponse(userName) {
        override  fun message(): String = "/timeout $userName $timeoutDuration"
    }
    class Text(userName: String, private val responseText: String): ModerationResponse(userName) {
        override fun message(): String = responseText.format(userName)
    }
    abstract fun message():String
}