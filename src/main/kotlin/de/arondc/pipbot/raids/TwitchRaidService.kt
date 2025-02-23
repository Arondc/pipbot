package de.arondc.pipbot.raids

import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.channels.ShoutoutOnRaidType
import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.SendMessageEvent
import de.arondc.pipbot.events.TwitchRaidEvent
import de.arondc.pipbot.services.LanguageService
import de.arondc.pipbot.twitch.TwitchStreamService
import org.springframework.stereotype.Service

@Service
class TwitchRaidService(
    private val channelService: ChannelService,
    private val twitchStreamService: TwitchStreamService,
    private val languageService: LanguageService,
    private val eventPublisher: EventPublishingService
    ) {

    fun processRaid(twitchRaidEvent: TwitchRaidEvent) {
        val channel = channelService.findByNameIgnoreCase(twitchRaidEvent.raidedChannel)
        if (channel.shoutoutOnRaid == ShoutoutOnRaidType.NONE ||
            channelIsNotInShoutoutWhitelist(channel,twitchRaidEvent))
        {
            return
        }

        when (channel.shoutoutOnRaid) {
            ShoutoutOnRaidType.TEXT -> reactByText(twitchRaidEvent)
            ShoutoutOnRaidType.TWITCH_SHOUTOUT -> reactByTwitchShoutout(twitchRaidEvent)
            ShoutoutOnRaidType.STREAM_ELEMENTS_SHOUTOUT -> reactByStreamElementsShoutout(twitchRaidEvent)
            else -> return
        }
    }

    private fun channelIsNotInShoutoutWhitelist(
        channel: ChannelEntity,
        twitchRaidEvent: TwitchRaidEvent
    ) = channel.automatedShoutoutChannels.isNotEmpty() &&
            channel.automatedShoutoutChannels.any {
                it.equals(
                    other = twitchRaidEvent.incomingRaider,
                    ignoreCase = true
                )
            }

    private fun reactByText(twitchRaidEvent: TwitchRaidEvent) {
        val lastGame = twitchStreamService.findLastGameFor(twitchRaidEvent.incomingRaider)
        val message = languageService.getMessage(
            twitchRaidEvent.raidedChannel,
            "twitch.raid.message",
            arrayOf(twitchRaidEvent.incomingRaider, twitchRaidEvent.size, lastGame)
        )
        eventPublisher.publishEvent(SendMessageEvent(twitchRaidEvent.raidedChannel, message))
    }

    private fun reactByTwitchShoutout(twitchRaidEvent: TwitchRaidEvent) {
        twitchStreamService.shoutout(twitchRaidEvent.raidedChannel, twitchRaidEvent.incomingRaider)
    }

    private fun reactByStreamElementsShoutout(twitchRaidEvent: TwitchRaidEvent) {
        eventPublisher.publishEvent(
            SendMessageEvent(
                twitchRaidEvent.raidedChannel,
                "!so ${twitchRaidEvent.incomingRaider}"
            )
        )
    }


}