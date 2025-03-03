package de.arondc.pipbot.moderation

import com.ninjasquad.springmockk.MockkBean
import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.channels.ShoutoutOnRaidType
import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.ModerationActionEvent
import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchPermission
import de.arondc.pipbot.userchannelinformation.UserChannelInformationService
import de.arondc.pipbot.userchannelinformation.UserInformation
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.event.RecordApplicationEvents
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.util.*

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [ModerationService::class, ModerationResponseStorage::class, EventPublishingService::class])
@RecordApplicationEvents
class ModerationServiceTest {

    @MockkBean
    lateinit var userChannelInformationService: UserChannelInformationService

    @MockkBean
    lateinit var eventPublisher: EventPublishingService

    @MockkBean
    lateinit var moderationResponseStorage: ModerationResponseStorage

    @Autowired
    lateinit var moderationService: ModerationService


    companion object {
        const val CHANNEL_NAME = "channel"
        const val USER_NAME = "user"

        val CHANNEL_ENTITY = ChannelEntity(
            name = CHANNEL_NAME,
            language = Locale.ENGLISH,
            shoutoutOnRaid = ShoutoutOnRaidType.NONE,
            automatedShoutoutChannels = emptyList(),
            active = true,
            id = 100
        )

        private val NEW_USER = buildUser(followDate = null)
        private val FOLLOWER_LESS_THAN_6_MONTHS = buildUser(
            followDate = LocalDateTime.now().minusMonths(6).plusDays(1)
        )
        private val FOLLOWER_LESS_THAN_12_MONTHS = buildUser(followDate = LocalDateTime.now().minusMonths(6))
        private val FOLLOWER_MORE_THAN_12_MONTHS = buildUser(followDate = LocalDateTime.now().minusMonths(12))

        @JvmStatic
        fun provideUsersWithDifferentAges() = arrayOf(
            arrayOf("Viewer", NEW_USER, "moderated viewer"),
            arrayOf("New follower", FOLLOWER_LESS_THAN_6_MONTHS, "moderated new follower"),
            arrayOf("Short term follower", FOLLOWER_LESS_THAN_12_MONTHS, "moderated short term follower"),
            arrayOf("Long term follower", FOLLOWER_MORE_THAN_12_MONTHS, "moderated long term follower"),
        )

        @JvmStatic
        fun provideUserTwitchPermissionResponses() = arrayOf(
            arrayOf("Normal User", buildUser(TwitchPermission.EVERYONE), 1),
            arrayOf("Subscriber", buildUser(TwitchPermission.SUBSCRIBER), 1),
            arrayOf("Moderator", buildUser(TwitchPermission.MODERATOR), 0),
            arrayOf("Broadcaster", buildUser(TwitchPermission.BROADCASTER), 0),
            arrayOf("Owner", buildUser(TwitchPermission.OWNER), 0),
        )

        private fun buildUser(
            highestPermission: TwitchPermission = TwitchPermission.EVERYONE, followDate: LocalDateTime? = null
        ) = UserInformation(
            userName = USER_NAME,
            channel = CHANNEL_ENTITY,
            lastSeen = LocalDateTime.now(),
            amountOfVisitedStreams = 0,
            highestTwitchUserLevel = highestPermission,
            followerSince = followDate,
            followerVerifiedOnce = true,
        )
    }

    @ParameterizedTest(name = "{index}. {0} is moderated with response '{2}'")
    @MethodSource("provideUsersWithDifferentAges")
    fun `Users are moderated differently according to their follow age`(
        userType: String, userChannelInformationEntity: UserInformation, expectedModerationMessage: String
    ) {

        every {
            userChannelInformationService.getUserChannelInformation(
                userName = USER_NAME,
                channelName = CHANNEL_NAME,
            )
        } returns userChannelInformationEntity

        every { eventPublisher.publishEvent(any()) } just runs

        val trustLevel = slot<UserTrustLevel>()
        every { moderationResponseStorage.findByChannelAndTrustLevel(CHANNEL_ENTITY, capture(trustLevel)) } answers {
            ModerationResponseEntity(
                CHANNEL_ENTITY, trustLevel.captured, ModerationResponeType.TEXT, text = when (trustLevel.captured) {
                    UserTrustLevel.VIEWER -> "moderated viewer"
                    UserTrustLevel.NEW_FOLLOWER -> "moderated new follower"
                    UserTrustLevel.SHORT_TERM_FOLLOWER -> "moderated short term follower"
                    UserTrustLevel.LONG_TERM_FOLLOWER -> "moderated long term follower"
                }
            )
        }

        moderationService.moderate(
            ModerationActionEvent(
                channel = CHANNEL_NAME,
                user = USER_NAME,
            )
        )

        verify(exactly = 1) {
            eventPublisher.publishEvent(
                withArg {
                    assertAll(
                        { assertThat(it).isExactlyInstanceOf(SendMessageEvent::class.java) },
                        { assertThat((it as SendMessageEvent).channel).isEqualTo(CHANNEL_ENTITY.name) },
                        { assertThat((it as SendMessageEvent).message).isEqualTo(expectedModerationMessage) },
                    )
                }
            )
        }
    }

    @Test
    fun `A configured ban is sending a ban message for the given user in the given channel`() {
        every { eventPublisher.publishEvent(any()) } just runs
        every {
            userChannelInformationService.getUserChannelInformation(
                userName = USER_NAME,
                channelName = CHANNEL_NAME,
            )
        } returns buildUser()

        every {
            moderationResponseStorage.findByChannelAndTrustLevel(
                CHANNEL_ENTITY,
                UserTrustLevel.VIEWER
            )
        } returns ModerationResponseEntity(CHANNEL_ENTITY, UserTrustLevel.VIEWER, ModerationResponeType.BAN)

        moderationService.moderate(
            ModerationActionEvent(
                channel = CHANNEL_NAME,
                user = USER_NAME,
            )
        )

        verify(exactly = 1) {
            eventPublisher.publishEvent(
                withArg {
                    assertAll(
                        { assertThat(it).isExactlyInstanceOf(SendMessageEvent::class.java) },
                        { assertThat((it as SendMessageEvent).channel).isEqualTo(CHANNEL_ENTITY.name) },
                        { assertThat((it as SendMessageEvent).message).isEqualTo("/ban $USER_NAME") },
                    )
                }
            )
        }
    }

    @Test
    fun `A configured timeout is sending a timeout message for the given user in the given channel`() {
        every { eventPublisher.publishEvent(any()) } just runs
        every {
            userChannelInformationService.getUserChannelInformation(
                userName = USER_NAME,
                channelName = CHANNEL_NAME,
            )
        } returns buildUser()

        every {
            moderationResponseStorage.findByChannelAndTrustLevel(
                CHANNEL_ENTITY,
                UserTrustLevel.VIEWER
            )
        } returns ModerationResponseEntity(
            CHANNEL_ENTITY,
            UserTrustLevel.VIEWER,
            ModerationResponeType.TIMEOUT,
            duration = 1234
        )

        moderationService.moderate(
            ModerationActionEvent(
                channel = CHANNEL_NAME,
                user = USER_NAME,
            )
        )

        verify(exactly = 1) {
            eventPublisher.publishEvent(
                withArg {
                    assertAll(
                        { assertThat(it).isExactlyInstanceOf(SendMessageEvent::class.java) },
                        { assertThat((it as SendMessageEvent).channel).isEqualTo(CHANNEL_ENTITY.name) },
                        { assertThat((it as SendMessageEvent).message).isEqualTo("/timeout $USER_NAME 1234") },
                    )
                }
            )
        }
    }

    @Test
    fun `A configured text is sending a text message for the given user in the given channel`() {
        every { eventPublisher.publishEvent(any()) } just runs
        every {
            userChannelInformationService.getUserChannelInformation(
                userName = USER_NAME,
                channelName = CHANNEL_NAME,
            )
        } returns buildUser()

        every {
            moderationResponseStorage.findByChannelAndTrustLevel(
                CHANNEL_ENTITY,
                UserTrustLevel.VIEWER
            )
        } returns ModerationResponseEntity(CHANNEL_ENTITY, UserTrustLevel.VIEWER, ModerationResponeType.TEXT, text = "you're a bad user %s")

        moderationService.moderate(
            ModerationActionEvent(
                channel = CHANNEL_NAME,
                user = USER_NAME,
            )
        )

        verify(exactly = 1) {
            eventPublisher.publishEvent(
                withArg {
                    assertAll(
                        { assertThat(it).isExactlyInstanceOf(SendMessageEvent::class.java) },
                        { assertThat((it as SendMessageEvent).channel).isEqualTo(CHANNEL_ENTITY.name) },
                        { assertThat((it as SendMessageEvent).message).isEqualTo("you're a bad user $USER_NAME") },
                    )
                }
            )
        }
    }

    @Test
    fun `If no moderation configuration is set for the given channel and trust level, no moderation happens`() {
        every {
            userChannelInformationService.getUserChannelInformation(
                userName = USER_NAME,
                channelName = CHANNEL_NAME,
            )
        } returns buildUser()

        every {
            moderationResponseStorage.findByChannelAndTrustLevel(
                CHANNEL_ENTITY,
                UserTrustLevel.VIEWER
            )
        } returns null

        moderationService.moderate(
            ModerationActionEvent(
                channel = CHANNEL_NAME,
                user = USER_NAME,
            )
        )

        verify(exactly = 0) { eventPublisher.publishEvent(any<SendMessageEvent>()) }
    }

    @ParameterizedTest(name = "{index}. {0} {2,choice,0#is not|1#is} moderated")
    @MethodSource("provideUserTwitchPermissionResponses")
    fun `Users are moderated differently according to their highest Twitch Permission`(
        userType: String,
        userChannelInformationEntity: UserInformation,
        expectedEventCount: Int
    ) {
        every { eventPublisher.publishEvent(any()) } just runs
        every {
            userChannelInformationService.getUserChannelInformation(
                userName = USER_NAME,
                channelName = CHANNEL_NAME,
            )
        } returns userChannelInformationEntity

        val slotTrustLevel = slot<UserTrustLevel>()
        every {
            moderationResponseStorage.findByChannelAndTrustLevel(
                CHANNEL_ENTITY, capture(slotTrustLevel)
            )
        } answers {
            ModerationResponseEntity(
                CHANNEL_ENTITY, slotTrustLevel.captured, ModerationResponeType.TEXT, text = "Dummy Text"
            )
        }

        moderationService.moderate(
            ModerationActionEvent(
                channel = CHANNEL_NAME,
                user = USER_NAME,
            )
        )

        verify(exactly = expectedEventCount) { eventPublisher.publishEvent(any<SendMessageEvent>()) }
    }

    @Test
    fun `If the user is not found an exception is thrown`() {
        every { userChannelInformationService.getUserChannelInformation(any(), any()) } returns null

        val exception = assertThrows<RuntimeException> {
            moderationService.moderate(
                ModerationActionEvent(
                    channel = CHANNEL_NAME, user = USER_NAME
                )
            )
        }
        verify(exactly = 5) { userChannelInformationService.getUserChannelInformation(USER_NAME, CHANNEL_NAME) }
        assertThat(exception).message().isEqualTo("Nutzer unbekannt")
    }

    @Test
    fun `If the user is not found there is a retry in getting the user information`() {
        every { eventPublisher.publishEvent(any()) } just runs
        every {
            userChannelInformationService.getUserChannelInformation(
                userName = USER_NAME,
                channelName = CHANNEL_NAME,
            )
        } returns null andThen null andThen null andThen null andThen buildUser()

        every {
            moderationResponseStorage.findByChannelAndTrustLevel(
                CHANNEL_ENTITY,
                UserTrustLevel.VIEWER
            )
        } returns ModerationResponseEntity(CHANNEL_ENTITY, UserTrustLevel.VIEWER, ModerationResponeType.BAN)

        moderationService.moderate(
            ModerationActionEvent(
                channel = CHANNEL_NAME,
                user = USER_NAME,
            )
        )

        verify(exactly = 5) { userChannelInformationService.getUserChannelInformation(USER_NAME, CHANNEL_NAME) }
        verify(exactly = 1) {
            eventPublisher.publishEvent(
                withArg {
                    assertAll(
                        { assertThat(it).isExactlyInstanceOf(SendMessageEvent::class.java) },
                        { assertThat((it as SendMessageEvent).channel).isEqualTo(CHANNEL_ENTITY.name) },
                        { assertThat((it as SendMessageEvent).message).isEqualTo("/ban $USER_NAME") },
                    )
                }
            )
        }
    }
}
