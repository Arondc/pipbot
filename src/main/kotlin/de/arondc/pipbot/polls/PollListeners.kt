package de.arondc.pipbot.polls

import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.events.ProcessingEvent
import mu.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class PollListeners(
    val channelService: ChannelService,
    val pollService: PollService,
) {

    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    fun receiveMessage(processingEvent: ProcessingEvent) {
        when {
            //!poll "text"                                           -> Poll mit Standardwerten    (time = 3m, options = 1/2)
            //!poll [options=Option1,Option2,Option3,....] "text"    -> Poll mit Antwortoptionen   (options = 1/2)
            //!poll [time=5m] "text"                                 -> Poll mit beliebigem Teitlimit
            //!poll [open=true] "text"                               -> Poll mit offener Antwort
            //!poll options=1,2 time=5m "1=Dings 2=Bumms"

            //TODO !poll help
            processingEvent.messageInfo.text.equals("!poll", ignoreCase = true) -> {
                val pollParameters = buildPollParameters()
                pollService.createAndStartPoll(pollParameters, processingEvent.channel)
            }
            processingEvent.messageInfo.text.startsWith("!poll ") -> {
                val pollParameters = buildPollParameters(processingEvent.messageInfo.text.substringAfter("!poll "))
                pollService.createAndStartPoll(pollParameters, processingEvent.channel)
            }
            processingEvent.messageInfo.text.startsWith("?") -> {
                pollService.acceptAnswer(processingEvent.messageInfo.text.trimStart('?'), processingEvent.channel, processingEvent.userInfo.userName)
            }
        }
    }

    private fun buildPollParameters(arguments: String = ""): Map<String, String> {
        val pollParameters = defaultPollParameters.toMutableMap()
        var remainingArguments = arguments
        parameters.forEach {
            remainingArguments = parseArgument(remainingArguments, it, pollParameters)
        }

        val text = remainingArguments.trim()
        pollParameters["text"] = text

        log.info {
            """Incoming poll message: $arguments
            text=$text
            time=${pollParameters["time"]}
            options=${pollParameters["options"]}
            open=${pollParameters["open"]}
            """.trimIndent()
        }
        return pollParameters
    }

    private fun parseArgument(message: String, argumentName: String, pollParameters: MutableMap<String, String>): String {
        val value = extractArgumentValue(message,argumentName) ?: return message
        pollParameters[argumentName] = value
        return message.replace("$argumentName=$value", "")
    }

    private fun extractArgumentValue(message: String, argumentName: String): String? {
        val argumentStartIndex = message.indexOf("$argumentName=")
        if (argumentStartIndex == -1) return null

        val argumentEndIndex = message.indexOf(string = " ", startIndex = argumentStartIndex)
        val value = if (argumentEndIndex < 0) {
            message.substringAfter("$argumentName=")
        } else {
            message.substring(argumentStartIndex + "$argumentName=".length..<argumentEndIndex)
        }
        return value
    }

    companion object {
        val defaultPollParameters = mapOf("time" to "3m", "options" to "1,2", "text" to "", "open" to "false")
        val parameters = setOf("time", "options", "open")
    }
}
