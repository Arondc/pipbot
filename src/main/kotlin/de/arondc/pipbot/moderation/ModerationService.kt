package de.arondc.pipbot.moderation

import de.arondc.pipbot.core.Functions.retry
import de.arondc.pipbot.events.ModerationActionEvent
import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchPermission
import de.arondc.pipbot.users.UserService
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Service

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

        val trustLevel = when(userInformation.followerVerifiedOnce){
            true -> ModerationByFollowAge
            false -> ModerationByAttendance
        }.calculateUserTrustLevel(userInformation)

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