package de.arondc.pipbot.moderation

import com.ninjasquad.springmockk.MockkBean
import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.channels.ShoutoutOnRaidType
import de.arondc.pipbot.events.ModerationActionEvent
import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchPermission
import de.arondc.pipbot.users.UserEntity
import de.arondc.pipbot.users.UserInformation
import de.arondc.pipbot.users.UserService
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.event.ApplicationEvents
import org.springframework.test.context.event.RecordApplicationEvents
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.util.*

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [ModerationService::class])
@RecordApplicationEvents
class ModerationServiceTest{

    @MockkBean
    lateinit var userService: UserService

    @Autowired
    lateinit var moderationService: ModerationService

    @Autowired
    lateinit var applicationEvents: ApplicationEvents

    companion object {
        const val CHANNEL_NAME = "channel"
        const val USER_NAME = "user"

        const val BAN = "/ban $USER_NAME"
        const val HIGH_TIMEOUT = "/timeout $USER_NAME 600"
        const val LOW_TIMEOUT = "/timeout $USER_NAME 10"
        const val TEXT_MODERATION = "Hey, $USER_NAME bitte pass auf was du schreibst."

        val USER_ENTITY = UserEntity(
            name = USER_NAME,
            id = 1
        )

        val CHANNEL_ENTITY = ChannelEntity(
            name = CHANNEL_NAME,
            language = Locale.ENGLISH,
            shoutoutOnRaid = ShoutoutOnRaidType.NONE,
            automatedShoutoutChannels = emptyList(),
            active = true,
            id = 100
        )

        private val NEW_USER = buildUserWithFollowDate(null)
        private val FOLLOWER_LESS_THAN_6_MONTHS = buildUserWithFollowDate(
            LocalDateTime.now().minusMonths(6).plusMinutes(5))
        private val FOLLOWER_LESS_THAN_12_MONTHS = buildUserWithFollowDate(LocalDateTime.now().minusMonths(6))
        private val FOLLOWER_MORE_THAN_12_MONTHS = buildUserWithFollowDate(LocalDateTime.now().minusMonths(12))

        @JvmStatic
        fun provideUserModerationResponses() = arrayOf(
            arrayOf("New User", NEW_USER, BAN),
            arrayOf("Follower <6 months", FOLLOWER_LESS_THAN_6_MONTHS, HIGH_TIMEOUT),
            arrayOf("Follower <12 months", FOLLOWER_LESS_THAN_12_MONTHS, LOW_TIMEOUT),
            arrayOf("Follower >12 months", FOLLOWER_MORE_THAN_12_MONTHS, TEXT_MODERATION),
        )

        @JvmStatic
        fun provideUserTwitchPermissionResponses() = arrayOf(
            arrayOf("Normal User", buildUserWithPermission(TwitchPermission.EVERYONE), 1),
            arrayOf("Subscriber", buildUserWithPermission(TwitchPermission.SUBSCRIBER), 1),
            arrayOf("Moderator", buildUserWithPermission(TwitchPermission.MODERATOR), 0 ),
            arrayOf("Broadcaster", buildUserWithPermission(TwitchPermission.BROADCASTER), 0),
            arrayOf("Owner", buildUserWithPermission(TwitchPermission.OWNER), 0),
        )

        private fun buildUserWithPermission(highestPermission: TwitchPermission) = UserInformation(
            user = USER_ENTITY,
            channel = CHANNEL_ENTITY,
            lastSeen = LocalDateTime.now(),
            amountOfVisitedStreams = 0,
            highestTwitchUserLevel = highestPermission,
            followerSince = LocalDateTime.now(),
        )

        private fun buildUserWithFollowDate(followDate: LocalDateTime?) = UserInformation(
            user = USER_ENTITY,
            channel = CHANNEL_ENTITY,
            lastSeen = LocalDateTime.now(),
            amountOfVisitedStreams = 0,
            highestTwitchUserLevel = TwitchPermission.EVERYONE,
            followerSince = followDate,
        )
    }

    @ParameterizedTest(name = "{index}. {0} gets response \"{2}\"")
    @MethodSource("provideUserModerationResponses")
    fun `Users are moderated differently according to their follow age`(userType: String, userChannelInformationEntity: UserInformation, response: String) {
        every { userService.getUserChannelInformation(
            userName = USER_NAME,
            channelName = CHANNEL_NAME,
        ) } returns userChannelInformationEntity

        moderationService.processModerationActionEvent(ModerationActionEvent(
            channel = CHANNEL_NAME,
            user = USER_NAME,
        ))

        val sendMessageEvents = applicationEvents.stream(SendMessageEvent::class.java).toList()
        assertThat(sendMessageEvents).hasSize(1)
        assertThat(sendMessageEvents[0].channel).isEqualTo(CHANNEL_ENTITY.name)
        assertThat(sendMessageEvents[0].message).isEqualTo(response)
    }

    @ParameterizedTest(name = "{index}. {0} {2,choice,0#is not|1#is} moderated")
    @MethodSource("provideUserTwitchPermissionResponses")
    fun `Users are moderated differently according to their highest Twitch Permission`(userType:String, userChannelInformationEntity: UserInformation, expectedEventCount: Int) {
        every { userService.getUserChannelInformation(
            userName = USER_NAME,
            channelName = CHANNEL_NAME,
        ) } returns userChannelInformationEntity

        moderationService.processModerationActionEvent(ModerationActionEvent(
            channel = CHANNEL_NAME,
            user = USER_NAME,
        ))

        val sendMessageEvents = applicationEvents.stream(SendMessageEvent::class.java).toList()
        assertThat(sendMessageEvents).hasSize(expectedEventCount)

    }

    @Test
    fun `If the user is not found an exception is thrown`(){
        every { userService.getUserChannelInformation(any(), any()) } returns null

        val exception = assertThrows<RuntimeException> {
            moderationService.processModerationActionEvent(ModerationActionEvent(
                channel = CHANNEL_NAME,
                user = USER_NAME
            ))
        }
        assertThat(exception).message().isEqualTo("Nutzer unbekannt")
    }


}
