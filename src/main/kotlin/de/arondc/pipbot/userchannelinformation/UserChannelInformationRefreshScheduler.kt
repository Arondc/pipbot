package de.arondc.pipbot.userchannelinformation

import de.arondc.pipbot.channels.ChannelService
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@EnableScheduling
@Configuration
class SchedulerConfiguration

@Component
class UserChannelInformationRefreshScheduler(
    private val channelService: ChannelService,
    private val userChannelInformationService: UserChannelInformationService,
) {

    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.MINUTES)
    fun startRefreshUserLists() {
        channelService
            .findAll()
            .forEach { channel -> userChannelInformationService.updateUserListForChannel(channel.name) }
    }
}

