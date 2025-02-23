package de.arondc.pipbot.raids

import de.arondc.pipbot.events.TwitchRaidEvent
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class TwitchRaidProcessor(
    val twitchRaidService: TwitchRaidService
) {
    @ApplicationModuleListener
    fun receiveRaidEvent(twitchRaidEvent: TwitchRaidEvent) {
        twitchRaidService.processRaid(twitchRaidEvent)
    }
}
