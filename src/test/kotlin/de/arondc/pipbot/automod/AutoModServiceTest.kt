package de.arondc.pipbot.automod

import com.ninjasquad.springmockk.MockkBean
import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.channels.ShoutoutOnRaidType
import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.MessageInfo
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.repository.findByIdOrNull
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.event.RecordApplicationEvents
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.util.*

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [AutoModService::class, AutoModPhraseRepository::class, EventPublishingService::class])
@TestPropertySource(properties = ["automod.cache_size=3"])
@RecordApplicationEvents
class AutoModServiceTest {

    @MockkBean
    lateinit var autoModPhraseRepository : AutoModPhraseRepository

    @MockkBean
    lateinit var eventPublisher: EventPublishingService

    @Autowired
    lateinit var autoModService: AutoModService

    companion object {
        const val CHANNEL_NAME = "dummyChannel"
        const val USER_NAME = "dummyUser"
        const val BAD_PHRASE = "badphrase"

        val CHANNEL_ENTITY = ChannelEntity(
            name = CHANNEL_NAME,
            language = Locale.ENGLISH,
            shoutoutOnRaid = ShoutoutOnRaidType.NONE,
            automatedShoutoutChannels = emptyList(),
            active = true,
            id = 1
        )

        @JvmStatic
        fun provideBadPhrasedMessages() : List<MessageInfo> {
            return listOf(
                buildEventMessageInfo("bla badphrase bla", normalizedText = ""),
                buildEventMessageInfo("", normalizedText = "bla badphrase bla")
            )
        }

        private fun buildEventMessageInfo(text : String, normalizedText : String = text) = MessageInfo(
            text = text,
            normalizedText = normalizedText,
            hasLink = false,
        )
    }

    @ParameterizedTest
    @MethodSource("provideBadPhrasedMessages")
    fun `If message text contains a bad phrase then a moderation event is triggered`(eventMessageInfo : MessageInfo) {
        every { autoModPhraseRepository.findByChannelName("dummyChannel") } returns listOf(
            AutoModPhraseEntity(
                text = BAD_PHRASE,
                channel = CHANNEL_ENTITY
            )
        )

        every { eventPublisher.publishEvent(any()) } just Runs

        val result = autoModService.needsModeration(
            channelName = CHANNEL_ENTITY.name,
            userName = USER_NAME,
            messageInfo = eventMessageInfo
        )

        assertThat(result).isTrue()
    }

    @Test
    fun `If message text contains no bad phrase then no moderation event is triggered`() {
        val eventMessageInfo = buildEventMessageInfo("Something nice")

        every { autoModPhraseRepository.findByChannelName("dummyChannel") } returns listOf(
            AutoModPhraseEntity(
                text = BAD_PHRASE,
                channel = CHANNEL_ENTITY
            )
        )

        autoModService.needsModeration(
            channelName = CHANNEL_ENTITY.name,
            userName = USER_NAME,
            messageInfo = eventMessageInfo
        )

        verify(exactly = 0) { eventPublisher.publishEvent(any()) }
    }


    @Test
    fun processNewPhrase() {
        //GIVEN
        every { eventPublisher.publishEvent(any()) } just Runs
        every { autoModPhraseRepository.findByChannelName("dummyChannel") } returns listOf()
        autoModService.needsModeration(
            channelName = CHANNEL_ENTITY.name,
            userName = USER_NAME,
            messageInfo = buildEventMessageInfo("Something nice")
        )
        autoModService.needsModeration(
            channelName = CHANNEL_ENTITY.name,
            userName = USER_NAME,
            messageInfo = buildEventMessageInfo(BAD_PHRASE)
        )

        //WHEN
        val result = autoModService.processNewPhrase(CHANNEL_ENTITY.name, BAD_PHRASE)

        //THEN
        assertThat(result).containsOnly(USER_NAME)
    }

    @Test
    fun findAll() {
        every { autoModPhraseRepository.findAll() } returns listOf()
        autoModService.findAll()
        verify { autoModPhraseRepository.findAll() }
    }

    @Test
    fun save() {
        every { autoModPhraseRepository.save(any()) } returns AutoModPhraseEntity(text = BAD_PHRASE)
        autoModService.save(AutoModPhraseEntity(BAD_PHRASE, CHANNEL_ENTITY))
        verify{ autoModPhraseRepository.save(any()) }
    }

    @Test
    fun findById() {
        every { autoModPhraseRepository.findByIdOrNull(any()) } returns AutoModPhraseEntity(text = BAD_PHRASE, channel = CHANNEL_ENTITY)
        autoModService.findById(1)
        verify{ autoModPhraseRepository.findByIdOrNull(any()) }
    }

    @Test
    fun delete() {
        every { autoModPhraseRepository.delete(any()) } just Runs
        val autoModPhraseEntity = AutoModPhraseEntity(text = BAD_PHRASE, channel = CHANNEL_ENTITY)
        autoModService.delete(autoModPhraseEntity)
        verify { autoModPhraseRepository.delete(autoModPhraseEntity) }
    }


}