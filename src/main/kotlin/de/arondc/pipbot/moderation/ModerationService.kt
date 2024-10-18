package de.arondc.pipbot.moderation

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
class ModerationService(private val userService: UserService,private  val eventPublisher: ApplicationEventPublisher) {
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    fun processModerationActionEvent(event: ModerationActionEvent) {
        val userInformation = userService.getUserChannelInformation(event.user, event.channel)
            ?: throw RuntimeException("Nutzer unbekannt")

        if (TwitchPermission.MODERATOR.isSatisfiedBy(userInformation.highestTwitchUserLevel)) {
            return
        }

        val trustLevel = getUserTrustLevel(userInformation)
        val moderationResponse : ModerationResponse = when (trustLevel) {
            UserTrustLevel.VIEWER -> ModerationResponse.Ban(event.user)
            UserTrustLevel.NEW_FOLLOWER -> ModerationResponse.Timeout(event.user, 600)
            UserTrustLevel.SHORT_TERM_FOLLOWER -> ModerationResponse.Timeout(event.user, 10)
            UserTrustLevel.LONG_TERM_FOLLOWER -> ModerationResponse.Text(event.user, "Hey, %s bitte pass auf was du schreibst.")
        }

        if(moderationResponse is ModerationResponse.None){
            return
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
    class Timeout(userName: String, private val timeoutLength:Long): ModerationResponse(userName) {
        override  fun message(): String = "/timeout $userName $timeoutLength"
    }
    class Text(userName: String, private val responseText: String): ModerationResponse(userName) {
        override fun message(): String = responseText.format(userName)
    }
    class None: ModerationResponse() {
        override fun message(): String = ""
    }
    abstract fun message():String
}