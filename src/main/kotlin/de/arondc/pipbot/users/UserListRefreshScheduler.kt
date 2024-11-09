package de.arondc.pipbot.users

import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.events.UpdateUserListForChannelEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.TimeUnit

@EnableScheduling
@Configuration
class SchedulerConfiguration {}

@Component
class UserListRefreshScheduler(
    private val channelService: ChannelService,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Scheduled(fixedDelay = 30, timeUnit = TimeUnit.MINUTES)
    @Transactional
    fun startRefreshUserLists() {
        channelService
            .findAll()
            .map { channel -> UpdateUserListForChannelEvent(channel.name)}
            .forEach { event -> eventPublisher.publishEvent(event) }
    }
}

