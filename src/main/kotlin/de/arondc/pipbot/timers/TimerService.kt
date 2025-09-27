package de.arondc.pipbot.timers

import de.arondc.pipbot.events.CallType
import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.TwitchCallEvent
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import kotlin.concurrent.schedule
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.toJavaDuration
import kotlin.time.toKotlinDuration

typealias JavaDuration = java.time.Duration

@Service
class TimerService(
    val eventPublishingService: EventPublishingService,
) {
    companion object {
        val DEFAULT_TIMER_INTERVAL = 1.minutes.toJavaDuration()
    }

    fun createTimer(message: String, channel: String) {
        when {
            message.isEmpty() -> startTimer(DEFAULT_TIMER_INTERVAL, channel)
            else -> {
                val timerDuration = parseDuration(message, channel) ?: return
                startTimer(timerDuration, channel)
            }
        }
    }

    private fun parseDuration(message: String, channel: String): JavaDuration? = try {
        Duration.parse(message).toJavaDuration()
    } catch (_: IllegalArgumentException) {
        sendChatMessage(
            channel,
            "Zeitformat für Timer nicht korrekt. Beispiele für korrekte Angaben - 15s=15 Sekunden, 3m=3 Minuten, 1m15s=1 Minute 15 Sekunden"
        )
        null
    }

    private fun startTimer(timerDuration: JavaDuration, channel: String) {
        Timer().schedule(
            calculateTimerEnd(timerDuration)
        ) {
            sendChatMessage(channel, "Timer mit Dauer ${timerDuration.toKotlinDuration()} abgelaufen")
        }
        sendChatMessage(channel, "Timer mit Dauer ${timerDuration.toKotlinDuration()} gestartet")
    }

    private fun calculateTimerEnd(timerDuration: JavaDuration): Date = Date.from(Instant.now().plus(timerDuration))

    private fun sendChatMessage(
        channel: String, message: String
    ) {
        eventPublishingService.publishEvent(
            TwitchCallEvent(
                CallType.SEND_MESSAGE, channel, message
            )
        )
    }
}

