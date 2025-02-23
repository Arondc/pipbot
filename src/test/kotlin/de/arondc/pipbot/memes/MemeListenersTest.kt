package de.arondc.pipbot.memes

import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.channels.ShoutoutOnRaidType
import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchMessageEvent
import de.arondc.pipbot.events.TwitchMessageEvent.MessageInfo
import de.arondc.pipbot.events.TwitchMessageEvent.UserInfo
import de.arondc.pipbot.services.LanguageService
import de.arondc.pipbot.streams.StreamEntity
import de.arondc.pipbot.streams.StreamService
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.context.ApplicationEventPublisher
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.*

@ExtendWith(MockKExtension::class)
class MemeListenersTest {

    companion object {
        const val RESPONSE_LOCALIZATION_KEY = "twitch.memes.acknowledge"
        const val CHANNEL_NAME = "dummyChannel"
        const val USER_NAME = "dummyUser"
        const val RESPONSE_MESSAGE_TEXT = "dummyResponse"

        val CHANNEL = ChannelEntity(
            name = CHANNEL_NAME,
            language = Locale.ENGLISH,
            shoutoutOnRaid = ShoutoutOnRaidType.NONE,
            automatedShoutoutChannels = emptyList(),
            active = true,
            id = 1
        )

        val STREAM = StreamEntity(
            startTimes = setOf(LocalDateTime.of(2024, 1, 1, 13, 30)),
            channel = CHANNEL,
        )

        val EVENT_USER_INFO = UserInfo(
            userName = USER_NAME,
            permissions = setOf(),
            subscriberMonths = 0,
            subscriptionTier = 0
        )
    }

    @MockK
    lateinit var memeService: MemeService

    @MockK
    lateinit var languageService: LanguageService

    @MockK
    lateinit var channelService: ChannelService

    @MockK
    lateinit var streamService: StreamService

    @MockK
    lateinit var publisher: ApplicationEventPublisher

    @InjectMockKs
    lateinit var memeListeners: MemeListeners

    @Test
    fun `meme is processed successfully whenever the message starts with !meme`() {
        //Given
        val capturedMeme = slot<MemeEntity>()
        val capturedSendMessageEvent = slot<SendMessageEvent>()

        every { channelService.findByNameIgnoreCase(CHANNEL_NAME) } returns CHANNEL
        every { streamService.findOrPersistCurrentStream(CHANNEL_NAME) } returns STREAM
        every { memeService.save(capture(capturedMeme)) } answers { capturedMeme.captured }
        every {
            languageService.getMessage(CHANNEL_NAME, RESPONSE_LOCALIZATION_KEY, arrayOf(EVENT_USER_INFO.userName))
        } returns RESPONSE_MESSAGE_TEXT
        every { publisher.publishEvent(capture(capturedSendMessageEvent)) } just Runs

        //When
        val messageText = "dummyText"
        val twitchMessageEvent = buildTwitchMessageEvent("!meme $messageText")
        memeListeners.receiveMessage(twitchMessageEvent = twitchMessageEvent)

        //Then
        verify { channelService.findByNameIgnoreCase(CHANNEL_NAME) }
        verify { streamService.findOrPersistCurrentStream(CHANNEL_NAME) }
        verify { memeService.save(any()) }
        verify { languageService.getMessage(CHANNEL_NAME, RESPONSE_LOCALIZATION_KEY, arrayOf(EVENT_USER_INFO.userName)) }

        checkMeme(capturedMeme.captured, messageText)
        checkSendMessageEvent(capturedSendMessageEvent.captured)
    }

    @Test
    fun `meme is forwarded to the browser source whenever the message contains an imgflip link`() {
        val messageText = "https://imgflip.com/someawesomeaddress"
        //Given
        every { memeService.forwardMemeToBrowserSource(CHANNEL_NAME, messageText) } just Runs

        //When
        val twitchMessageEvent = buildTwitchMessageEvent(messageText)
        memeListeners.receiveBrowserSourceMessages(twitchMessageEvent = twitchMessageEvent)

        //Then
        verify { memeService.forwardMemeToBrowserSource(CHANNEL_NAME, messageText) }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "https://www.youtube.com/someawesomeaddress",
            "https://clips.twitch.tv/someawesomeaddress",
            "https://www.twitch.tv/aron_dc/clip/someawesomeaddress"
        ]
    )
    fun `meme is not forwarded to the browser source whenever the message contains a non-imgflip link`(address: String) {
        //When
        val twitchMessageEvent = buildTwitchMessageEvent(address)
        memeListeners.receiveBrowserSourceMessages(twitchMessageEvent = twitchMessageEvent)

        //Then
        verify { memeService wasNot Called }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "https://imgflip.com/someawesomeaddress",
            "https://www.youtube.com/someawesomeaddress",
            "https://clips.twitch.tv/someawesomeaddress",
            "https://www.twitch.tv/aron_dc/clip/someawesomeaddress",
            "https://www.twitch.tv/someother_channel/clip/someawesomeaddress",
            "HTTPS://WWW.TWITCH.TV/SOMEOTHER_CHANNEL/CLIP/SOMEAWESOMEADDRESS"
        ]
    )
    fun `meme is processed successfully whenever the message matches one of the meme sources`(messageText: String) {
        //Given
        val capturedMeme = slot<MemeEntity>()
        val capturedSendMessageEvent = slot<SendMessageEvent>()

        every { channelService.findByNameIgnoreCase(CHANNEL_NAME) } returns CHANNEL
        every { streamService.findOrPersistCurrentStream(CHANNEL_NAME) } returns STREAM
        every { memeService.save(capture(capturedMeme)) } answers { capturedMeme.captured }
        every {
            languageService.getMessage(CHANNEL_NAME, RESPONSE_LOCALIZATION_KEY, arrayOf(EVENT_USER_INFO.userName))
        } returns RESPONSE_MESSAGE_TEXT
        every { publisher.publishEvent(capture(capturedSendMessageEvent)) } just Runs

        //When
        val twitchMessageEvent = buildTwitchMessageEvent(messageText)
        memeListeners.receiveMessage(twitchMessageEvent = twitchMessageEvent)

        //Then
        verify { channelService.findByNameIgnoreCase(CHANNEL_NAME) }
        verify { streamService.findOrPersistCurrentStream(CHANNEL_NAME) }
        verify { memeService.save(any()) }
        verify { languageService.getMessage(CHANNEL_NAME, RESPONSE_LOCALIZATION_KEY, arrayOf(EVENT_USER_INFO.userName)) }

        checkMeme(capturedMeme.captured, messageText)
        checkSendMessageEvent(capturedSendMessageEvent.captured)
    }

    @Test
    fun `meme is not processed whenever the message matches does not match one the meme sources`() {
        //When
        memeListeners.receiveMessage(twitchMessageEvent = buildTwitchMessageEvent("https://www.google.com"))

        //Then
        verify { channelService wasNot Called }
        verify { streamService wasNot Called }
        verify { memeService wasNot Called }
        verify { languageService wasNot Called }
    }

    private fun checkSendMessageEvent(event: SendMessageEvent) = assertAll("SendMessageEvent fields",
        { assertThat(event).isNotNull },
        { assertThat(event.channel).isEqualTo(CHANNEL_NAME) },
        { assertThat(event.message).isEqualTo(RESPONSE_MESSAGE_TEXT) })

    private fun checkMeme(meme: MemeEntity, messageText: String) = assertAll("MemeEntity fields",
        { assertThat(meme).isNotNull },
        { assertThat(meme.recordedAt).isCloseTo(LocalDateTime.now(), within(3, ChronoUnit.SECONDS)) },
        { assertThat(meme.channel).isEqualTo(CHANNEL) },
        { assertThat(meme.sentByUser).isEqualTo(USER_NAME) },
        { assertThat(meme.message).isEqualTo(messageText) },
        { assertThat(meme.stream).isEqualTo(STREAM) }
    )

    private fun buildTwitchMessageEvent(message: String) = TwitchMessageEvent(
        channel = CHANNEL_NAME,
        userInfo = UserInfo(
            userName = USER_NAME,
            permissions = emptySet(),
            subscriberMonths = 0,
            subscriptionTier = 0
        ),
        messageInfo = MessageInfo(
            text = message,
            normalizedText = message,
            hasLink = false
        ),
    )
}