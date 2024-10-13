package de.arondc.pipbot.moderation

import de.arondc.pipbot.events.ModerationActionEvent
import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchPermission
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

        when {
            userInformation.followerSince != null && ChronoUnit.MONTHS.between(
                userInformation.followerSince, LocalDateTime.now()
            ) >= 6 -> {
                log.info { "Actioning on Moderation event for ${event.user} in ${event.channel}" }
                eventPublisher.publishEvent(
                    SendMessageEvent(
                        channel = event.channel, message = "/timeout ${event.user} 1"
                    )
                )
            }

            else -> {
                log.info { "Actioning on Moderation event for ${event.user} in ${event.channel}" }
                eventPublisher.publishEvent(
                    SendMessageEvent(
                        channel = event.channel, message = "/timeout ${event.user} 600"
                    )
                )
            }
        }
    }

}