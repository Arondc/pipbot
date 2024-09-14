package de.arondc.pipbot.polls

import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.events.TwitchMessageEvent
import mu.KotlinLogging
import org.springframework.modulith.events.ApplicationModuleListener
import org.springframework.stereotype.Component

@Component
class PollProcessor(
    val channelService: ChannelService,
    val pollService: PollService,
) {

    private val log = KotlinLogging.logger {}

    @ApplicationModuleListener
    fun receiveMessage(twitchMessageEvent: TwitchMessageEvent) {
        when {
            //!poll "text"                                           -> Poll mit Standardwerten    (time = 3m, options = 1/2)
            //!poll [options=Option1,Option2,Option3,....] "text"    -> Poll mit Antwortoptionen   (options = 1/2)
            //!poll [time=5m] "text"                                 -> Poll mit beliebigem Teitlimit
            //!poll [open=true] "text"                               -> Poll mit offener Antwort
            //!poll options=1,2 time=5m "1=Dings 2=Bumms"

            //TODO !poll help
            twitchMessageEvent.messageInfo.text.equals("!poll", ignoreCase = true) -> {
                val pollParameters = buildPollParameters()
                pollService.createAndStartPoll(pollParameters, twitchMessageEvent.channel)
            }
            twitchMessageEvent.messageInfo.text.startsWith("!poll ") -> {
                val pollParameters = buildPollParameters(twitchMessageEvent.messageInfo.text.substringAfter("!poll "))
                pollService.createAndStartPoll(pollParameters, twitchMessageEvent.channel)
            }
            twitchMessageEvent.messageInfo.text.startsWith("?") -> {
                pollService.acceptAnswer(twitchMessageEvent.messageInfo.text.trimStart('?'), twitchMessageEvent.channel, twitchMessageEvent.userInfo.userName)
            }
        }
    }

    private fun buildPollParameters(arguments: String = ""): Map<String, String> {
        val pollParameters = mutableMapOf("time" to "3m", "options" to "1,2", "text" to "", "open" to "false")
        var remainingArguments = arguments
        remainingArguments = parseArgument(remainingArguments, "time", pollParameters)
        remainingArguments = parseArgument(remainingArguments, "options", pollParameters)
        remainingArguments = parseArgument(remainingArguments, "open", pollParameters)

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

    fun parseArgument(message: String, argumentName: String, pollParameters: MutableMap<String, String>): String {
        val argumentStartIndex = message.indexOf("$argumentName=")
        if (argumentStartIndex == -1) return message

        val argumentEndIndex = message.indexOf(string = " ", startIndex = argumentStartIndex)
        val value = if (argumentEndIndex < 0) {
            message.substringAfter("$argumentName=")
        } else {
            message.substring(argumentStartIndex + "$argumentName=".length..<argumentEndIndex)
        }
        pollParameters[argumentName] = value
        return message.replace("$argumentName=$value", "")
    }
}
