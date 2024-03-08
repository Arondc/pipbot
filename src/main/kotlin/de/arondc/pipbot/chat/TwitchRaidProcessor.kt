package de.arondc.pipbot.chat

import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.channels.ShoutoutOnRaidType
import de.arondc.pipbot.services.LanguageService
import de.arondc.pipbot.twitch.SendMessageEvent
import de.arondc.pipbot.twitch.TwitchRaidEvent
import de.arondc.pipbot.twitch.TwitchStreamService
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class TwitchRaidProcessor(
    val channelService: ChannelService,
    val twitchStreamService: TwitchStreamService,
    val languageService: LanguageService,
    val applicationEventPublisher: ApplicationEventPublisher
) {
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    fun receiveRaidEvent(twitchRaidEvent: TwitchRaidEvent) {
        val channel = channelService.findByNameIgnoreCase(twitchRaidEvent.raidedChannel)!!
        if (channel.automatedShoutoutChannels.isNotEmpty() && !channel.automatedShoutoutChannels.contains(
                twitchRaidEvent.incomingRaider
            )
        ) {
            return
        }

        val sendMessageEvent: SendMessageEvent? = when (channel.shoutoutOnRaid) {
            ShoutoutOnRaidType.NONE -> null
            ShoutoutOnRaidType.TEXT -> buildTextMessage(twitchRaidEvent)
            ShoutoutOnRaidType.TWITCH_SHOUTOUT -> {
                sendShoutoutViaTwitch(twitchRaidEvent)
                null
            }

            ShoutoutOnRaidType.STREAM_ELEMENTS_SHOUTOUT -> sendStreamElementsShoutoutCommand(twitchRaidEvent)
        }

        if (sendMessageEvent != null) {
            applicationEventPublisher.publishEvent(sendMessageEvent)
        }

    }

    private fun buildTextMessage(twitchRaidEvent: TwitchRaidEvent): SendMessageEvent {
        val lastGame = twitchStreamService.findLastGameFor(twitchRaidEvent.incomingRaider)

        val message = languageService.getMessage(
            twitchRaidEvent.raidedChannel,
            "twitch.raid.message",
            arrayOf(twitchRaidEvent.incomingRaider, twitchRaidEvent.size, lastGame)
        )

        return SendMessageEvent(twitchRaidEvent.raidedChannel, message)
    }

    private fun sendShoutoutViaTwitch(twitchRaidEvent: TwitchRaidEvent) {
        twitchStreamService.shoutout(twitchRaidEvent.raidedChannel, twitchRaidEvent.incomingRaider)
    }

    private fun sendStreamElementsShoutoutCommand(twitchRaidEvent: TwitchRaidEvent): SendMessageEvent {
        return SendMessageEvent(twitchRaidEvent.raidedChannel, "!so ${twitchRaidEvent.incomingRaider}")
    }
}
