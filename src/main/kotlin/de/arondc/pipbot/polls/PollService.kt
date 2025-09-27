package de.arondc.pipbot.polls

import de.arondc.pipbot.events.CallType
import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.TwitchCallEvent
import de.arondc.pipbot.services.LanguageService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException
import java.util.*
import kotlin.concurrent.schedule

// TODO [Kommandos der Module sammeln, sodass es keine Überschneidungen gibt]
// TODO Internationalisierung der GUI per Konfiguration/Link umschaltbar machen

@Service
class PollService(
    private val languageService: LanguageService,
    private val eventPublisher: EventPublishingService
) {
    private val log = KotlinLogging.logger {}

    val polls = mutableMapOf<String, MutableSet<Poll>>()
    val answers = mutableMapOf<Poll, MutableMap<String, String>>()

    fun createAndStartPoll(pollParameters: Map<String, String>, channelName: String) {
        try {
            val configOpenPoll = pollParameters.getValue("open").toBoolean()
            val options = when (configOpenPoll) {
                true -> setOf()
                false -> pollParameters["options"]!!.split(",").toSet()
            }

            val poll = Poll(
                pollParameters["text"]!!, options, LocalDateTime.now(), LocalDateTime.now().plus(
                    Duration.parse("PT${pollParameters["time"]}")
                )
            )
            validatePoll(poll, channelName, configOpenPoll)

            startPoll(poll, channelName)
            notifyChatAboutPollStart(poll, channelName)

            log.info { poll }
            log.info { "timer ends at ${Date.from(poll.endTime.atZone(ZoneOffset.systemDefault()).toInstant())}" }

            runStopTimerForPoll(poll, channelName)
        } catch (ex: PollCreationException) {
            eventPublisher.publishEvent(
                TwitchCallEvent(
                    CallType.SEND_MESSAGE,
                    channelName, ex.msg
                )
            )
        } catch (_: DateTimeParseException) {
            val errMsg = languageService.getMessage(
                channelName, "polls.error.parameter.time", arrayOf(pollParameters.getValue("time"))
            )
            eventPublisher.publishEvent(
                TwitchCallEvent(CallType.SEND_MESSAGE, channelName, errMsg)
            )
        }
    }

    private fun notifyChatAboutPollStart(poll: Poll, channelName: String) {
        val startMessage = when (poll.isOpenPoll()) {
            true -> languageService.getMessage(channelName, "polls.poll.started.open")
            false -> languageService.getMessage(
                channelName, "polls.poll.started.answers", arrayOf(poll.options.joinToString(" ") { "?$it" })
            )
        }
        //TODO: Einmal debuggen und schauen ob wir hier pollPublisher nutzen müssen
        eventPublisher.publishEvent(TwitchCallEvent(CallType.SEND_MESSAGE, channelName, startMessage))
    }

    fun acceptAnswer(message: String, channelName: String, userName: String) {
        val pollsOfChannel = polls.getOrDefault(channelName, mutableSetOf())
        if (pollsOfChannel.isEmpty()) {
            return
        }

        val foundPoll = pollsOfChannel.find { poll -> poll.hasMatchingOption(message) || poll.isOpenPoll() }
        if (foundPoll == null) {
            return
        }

        countAnswer(foundPoll, userName, message)
        val response = languageService.getMessage(channelName, "polls.poll.countedAnswer", arrayOf(userName))
        eventPublisher.publishEvent(TwitchCallEvent(CallType.SEND_MESSAGE, channelName, response))
    }

    fun closePoll(poll: Poll, channelName: String) {
        val results: String = when (poll.isOpenPoll()) {
            true -> countAnswersForOpenPoll(poll)
            false -> countAnswersForPollWithOptions(poll)
        }
        val title = poll.text.ifBlank { "" }
        val message = languageService.getMessage(channelName, "polls.poll.result", arrayOf(title, results))
        val twitchCallEvent = TwitchCallEvent(CallType.SEND_MESSAGE, channelName, message)
        eventPublisher.publishEvent(twitchCallEvent)
        polls[channelName]!!.remove(poll)
    }

    private fun countAnswersForPollWithOptions(poll: Poll) =
        poll.options.associateWith { option -> answers.getValue(poll).values.count { it == option } }.toList()
            .sortedByDescending { (_, value) -> value }.joinToString(", ") { (k, v) -> "${k}=${v}" }

    private fun countAnswersForOpenPoll(poll: Poll) =
        answers[poll]!!.asSequence().groupingBy { it.value }.eachCount().toList()
            .sortedByDescending { (_, value) -> value }.joinToString(", ") { (k, v) -> "${k}=${v}" }

    private fun validatePoll(poll: Poll, channelName: String, configuredForOpenPoll: Boolean) {
        if (!configuredForOpenPoll && poll.options.all { it.isBlank() }) {
            throw PollCreationException(languageService.getMessage(channelName, "polls.error.noOptionsGiven"))
        }
        if (pollsWithOverlappingOptionsExist(channelName, poll)) {
            throw PollCreationException(languageService.getMessage(channelName, "polls.error.overlappingOptions"))
        }
        if (runningOpenPollExists(channelName)) {
            throw PollCreationException(languageService.getMessage(channelName, "polls.error.openPollRunning"))
        }
        if (runningPollsExistAndNewPollIsOpen(channelName, poll)) {
            throw PollCreationException(languageService.getMessage(channelName, "polls.error.openPollNotStartable"))
        }
    }

    private fun runningPollsExistAndNewPollIsOpen(channelName: String, poll: Poll) =
        polls[channelName]?.isNotEmpty() == true && poll.isOpenPoll()

    private fun runningOpenPollExists(channelName: String) = polls[channelName]?.any { p -> p.isOpenPoll() } == true

    private fun pollsWithOverlappingOptionsExist(channelName: String, poll: Poll) =
        polls[channelName]?.flatMap { p -> p.options }?.any { poll.options.contains(it) } == true

    private fun startPoll(poll: Poll, channelName: String) {
        polls.putIfAbsent(channelName, mutableSetOf())
        polls[channelName]!!.add(poll)
        answers[poll] = mutableMapOf()
    }

    private fun runStopTimerForPoll(poll: Poll, channelName: String) {
        Timer(poll.text).schedule(Date.from(poll.endTime.atZone(ZoneOffset.systemDefault()).toInstant())) {
            closePoll(poll, channelName)
        }
    }

    private fun countAnswer(foundPoll: Poll, userName: String, answer: String) {
        val answersForPoll = answers.getValue(foundPoll)
        answersForPoll[userName] = when (foundPoll.hasOptions()) {
            true -> foundPoll.options.find { it.equals(answer, ignoreCase = true) }!!
            false -> answer
        }
    }

}

data class Poll(
    val text: String,
    val options: Set<String>,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
) {
    fun isOpenPoll(): Boolean = options.isEmpty()
    fun hasOptions(): Boolean = options.isNotEmpty()
    fun hasMatchingOption(option: String): Boolean = options.any { it.equals(option, ignoreCase = true) }
}

class PollCreationException(val msg: String) : RuntimeException()