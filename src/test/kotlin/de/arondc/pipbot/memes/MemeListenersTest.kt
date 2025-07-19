package de.arondc.pipbot.memes

import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.ProcessingEvent
import de.arondc.pipbot.events.TwitchMessageEvent.MessageInfo
import de.arondc.pipbot.events.TwitchMessageEvent.UserInfo
import de.arondc.pipbot.services.LanguageService
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@ExtendWith(MockKExtension::class)
class MemeListenersTest {

    companion object {
        const val RESPONSE_LOCALIZATION_KEY = "twitch.memes.acknowledge"
        const val CHANNEL_NAME = "dummyChannel"
        const val USER_NAME = "dummyUser"
        const val RESPONSE_MESSAGE_TEXT = "dummyResponse"

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
    lateinit var publisher: EventPublishingService

    @InjectMockKs
    lateinit var memeListeners: MemeListeners

    @Test
    fun `meme is processed successfully whenever the message starts with !meme`() {
        //Given
        every { memeService.processMemeMessage(any(), any(), any()) } just Runs
        every {
            languageService.getMessage(CHANNEL_NAME, RESPONSE_LOCALIZATION_KEY, arrayOf(EVENT_USER_INFO.userName))
        } returns RESPONSE_MESSAGE_TEXT
        every { publisher.publishEvent(any()) } just Runs

        //When
        val messageText = "dummyText"
        val twitchMessageEvent = buildProcessingEvent("!meme $messageText")
        memeListeners.receiveMessage(processingEvent = twitchMessageEvent)

        //Then
        verify { memeService.processMemeMessage(any(), any(), any()) }
        verify { languageService.getMessage(CHANNEL_NAME, RESPONSE_LOCALIZATION_KEY, arrayOf(EVENT_USER_INFO.userName)) }
    }

    @Test
    fun `meme is forwarded to the browser source whenever the message contains an imgflip link`() {
        val messageText = "https://imgflip.com/someawesomeaddress"
        //Given
        every { memeService.forwardMemeToBrowserSource(CHANNEL_NAME, messageText) } just Runs

        //When
        val twitchMessageEvent = buildProcessingEvent(messageText)
        memeListeners.receiveBrowserSourceMessages(processingEvent = twitchMessageEvent)

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
        val twitchMessageEvent = buildProcessingEvent(address)
        memeListeners.receiveBrowserSourceMessages(processingEvent = twitchMessageEvent)

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

        every { memeService.processMemeMessage(any(), any(), any()) } just Runs
        every {
            languageService.getMessage(CHANNEL_NAME, RESPONSE_LOCALIZATION_KEY, arrayOf(EVENT_USER_INFO.userName))
        } returns RESPONSE_MESSAGE_TEXT
        every { publisher.publishEvent(any()) } just Runs

        //When
        val twitchMessageEvent = buildProcessingEvent(messageText)
        memeListeners.receiveMessage(processingEvent = twitchMessageEvent)

        //Then
        verify { memeService.processMemeMessage(any(), any(), any()) }
        verify { languageService.getMessage(CHANNEL_NAME, RESPONSE_LOCALIZATION_KEY, arrayOf(EVENT_USER_INFO.userName)) }

    }

    @Test
    fun `meme is not processed whenever the message matches does not match one the meme sources`() {
        //When
        memeListeners.receiveMessage(processingEvent = buildProcessingEvent("https://www.google.com"))

        //Then
        verify { memeService wasNot Called }
        verify { languageService wasNot Called }
    }

    private fun buildProcessingEvent(message: String) = ProcessingEvent(
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