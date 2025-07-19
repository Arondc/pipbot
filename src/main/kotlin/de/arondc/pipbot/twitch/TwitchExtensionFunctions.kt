package de.arondc.pipbot.twitch

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import de.arondc.pipbot.events.MessageEvent
import de.arondc.pipbot.events.MessageInfo
import de.arondc.pipbot.events.TwitchPermission
import de.arondc.pipbot.events.TwitchUserInfo
import java.text.Normalizer

fun ChannelMessageEvent.toMessageEvent() : MessageEvent {
    fun normalizeMessage(message: String) =
        Normalizer.normalize(message, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")

    fun messageContainsLink(message: String): Boolean {
        val couldBeATopLevelDomainEnding = """\S+\.\S{2,3}\b"""
        return message.contains("""http(s)?://""".toRegex()) ||
                message.contains(couldBeATopLevelDomainEnding.toRegex())
    }

    return MessageEvent(
        channel = this.channel.name,
        userInfo = TwitchUserInfo(
            userName = this.user.name,
            permissions = this.permissions.map { TwitchPermission.valueOf(it.name) }.toSet(),
            subscriberMonths = this.subscriberMonths,
            subscriptionTier = this.subscriptionTier
        ),
        messageInfo = MessageInfo(
            text = this.message,
            normalizedText = normalizeMessage(this.message),
            hasLink = messageContainsLink(this.message),
        ),
    )
}
