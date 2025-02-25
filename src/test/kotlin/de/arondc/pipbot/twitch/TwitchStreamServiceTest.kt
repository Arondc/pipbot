package de.arondc.pipbot.twitch

import com.github.twitch4j.helix.domain.InboundFollow
import com.github.twitch4j.helix.domain.InboundFollowers
import com.ninjasquad.springmockk.MockkBean
import de.arondc.pipbot.features.Feature
import de.arondc.pipbot.features.FeatureService
import de.arondc.pipbot.twitch.domain.TwitchScope
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Instant

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [TwitchStreamService::class])
class TwitchStreamServiceTest {

    @MockkBean
    lateinit var twitchConnector: TwitchConnector

    @MockkBean
    lateinit var publisher: ApplicationEventPublisher

    @MockkBean
    lateinit var featureService: FeatureService

    @Autowired
    lateinit var twitchStreamService: TwitchStreamService

    @Test
    fun `getFollowerInfoFor - when bot is unauthorized then deactivate feature for determining follow age`() {
        every { featureService.isEnabled(Feature.UpdateFollowerStatus) } returns true
        justRun { featureService.disable(Feature.UpdateFollowerStatus) }
        every { twitchConnector.getChannelFollowers(any(), any()) } throws TwitchConnector.MissingScopeException(
            TwitchScope.MODERATOR_READ_FOLLOWERS)

        val actual = twitchStreamService.getFollowerSince(channelName = "twitchChannel", userName = "user")

        Assertions.assertNull(actual)
        verify { featureService.disable(Feature.UpdateFollowerStatus) }
    }

    @Test
    fun `getFollowerInfoFor - when followList is null, null is returned`() {
        every { featureService.isEnabled(Feature.UpdateFollowerStatus) } returns true

        val returnedInboundFollowers = mockk<InboundFollowers>()
        every { returnedInboundFollowers.follows } returns null
        every { twitchConnector.getChannelFollowers(any(), any()) } returns returnedInboundFollowers

        val actual = twitchStreamService.getFollowerSince(channelName = "twitchChannel", userName = "user")

        Assertions.assertNull(actual)
        verify(exactly = 0) { featureService.disable(Feature.UpdateFollowerStatus) }
    }

    @Test
    fun `getFollowerInfoFor - when followList is empty, null is returned`() {
        every { featureService.isEnabled(Feature.UpdateFollowerStatus) } returns true

        val returnedInboundFollowers = mockk<InboundFollowers>()
        every { returnedInboundFollowers.follows } returns listOf()
        every { twitchConnector.getChannelFollowers(any(), any()) } returns returnedInboundFollowers

        val actual = twitchStreamService.getFollowerSince(channelName = "twitchChannel", userName = "user")

        Assertions.assertNull(actual)
        verify(exactly = 0) { featureService.disable(Feature.UpdateFollowerStatus) }
    }

    @Test
    fun `getFollowerInfoFor - when followList contains at least one entry, the first Instant is returned`() {
        every { featureService.isEnabled(Feature.UpdateFollowerStatus) } returns true

        val inboundFollow1 = mockk<InboundFollow>()
        val expectedFollowAt = Instant.parse("2020-04-02T00:00:00.000Z")
        every { inboundFollow1.followedAt } returns expectedFollowAt
        val inboundFollow2 = mockk<InboundFollow>()
        every { inboundFollow2.followedAt } returns null

        val returnedInboundFollowers = mockk<InboundFollowers>()
        every { returnedInboundFollowers.follows } returns listOf(inboundFollow1, inboundFollow2)
        every { twitchConnector.getChannelFollowers(any(), any()) } returns returnedInboundFollowers

        val actual = twitchStreamService.getFollowerSince(channelName = "twitchChannel", userName = "user")

        Assertions.assertEquals(expectedFollowAt, actual)
        verify(exactly = 0) { featureService.disable(Feature.UpdateFollowerStatus) }
    }
}