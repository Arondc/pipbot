package de.arondc.pipbot.quickban.frontend

import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.twitch.TwitchStreamService
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class QuickBanFrontendService(
    private val twitchStreamService: TwitchStreamService,
    private val channelService: ChannelService,
) {
    private val log = KotlinLogging.logger {}

    fun getChannels(): List<QuickBanChannelDTO> {
        return channelService.findAll().map { QuickBanChannelDTO(name = it.name) }.toList()
    }

    fun quickBan(request : QuickBanRequestDTO){
        log.info { "Quick ban requested through frontend channel=${request.channelName} users to ban=${request.userNames}" }
        twitchStreamService.banUsers(
            request.channelName,
            request.userNames.split(System.lineSeparator()).toSet()
        )
        }
    }


