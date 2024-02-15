package de.arondc.pipbot.processors

import de.arondc.pipbot.channels.ChannelRepository
import de.arondc.pipbot.channels.ShoutOutOnRaidType
import de.arondc.pipbot.services.LanguageService
import de.arondc.pipbot.streams.TwitchStreamService
import de.arondc.pipbot.twitch.SendMessageEvent
import de.arondc.pipbot.twitch.TwitchRaidEvent
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class TwitchRaidProcessor(
    val channelRepository: ChannelRepository,
    val twitchStreamService: TwitchStreamService,
    val languageService: LanguageService,
    val applicationEventPublisher: ApplicationEventPublisher
) {
    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    fun receiveRaidEvent(twitchRaidEvent: TwitchRaidEvent) {
        val channel = channelRepository.findByName(twitchRaidEvent.raidedChannel)!!
        if (channel.automatedShoutoutChannels.isNotEmpty() && !channel.automatedShoutoutChannels.contains(
                twitchRaidEvent.incomingRaider
            )
        ) {
            return
        }

        val sendMessageEvent : SendMessageEvent? =
        when(channel.shoutoutOnRaid){
            ShoutOutOnRaidType.NONE -> null
            ShoutOutOnRaidType.TEXT -> buildTextMessage(twitchRaidEvent)
            ShoutOutOnRaidType.TWITCH_SHOUTOUT -> {
                sendShoutoutViaTwitch(twitchRaidEvent)
                null
            }
            ShoutOutOnRaidType.STREAM_ELEMENTS_SHOUTOUT -> sendStreamElementsShoutoutCommand(twitchRaidEvent)
        }

        if(sendMessageEvent != null){
            applicationEventPublisher.publishEvent(sendMessageEvent)
        }

    }

    private fun buildTextMessage(twitchRaidEvent: TwitchRaidEvent) : SendMessageEvent{
        val lastGame = twitchStreamService.findLastGameFor(twitchRaidEvent.incomingRaider)

        val message = languageService.getMessage(
            twitchRaidEvent.raidedChannel,
            "twitch.raid.message",
            arrayOf(twitchRaidEvent.incomingRaider, twitchRaidEvent.size, lastGame)
        )

        return SendMessageEvent(twitchRaidEvent.raidedChannel, message)
    }

    private fun sendShoutoutViaTwitch(twitchRaidEvent: TwitchRaidEvent){
        twitchStreamService.shoutout(twitchRaidEvent.raidedChannel, twitchRaidEvent.incomingRaider)
    }

    private fun sendStreamElementsShoutoutCommand(twitchRaidEvent: TwitchRaidEvent): SendMessageEvent {
        return SendMessageEvent(twitchRaidEvent.raidedChannel, "!so ${twitchRaidEvent.incomingRaider}")
    }
}