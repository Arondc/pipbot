package de.arondc.pipbot.twitch

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import de.arondc.pipbot.events.TwitchMessageEvent
import de.arondc.pipbot.events.TwitchMessageEvent.MessageInfo
import de.arondc.pipbot.events.TwitchMessageEvent.UserInfo
import de.arondc.pipbot.events.TwitchPermission
import java.text.Normalizer

fun ChannelMessageEvent.toTwitchMessageEvent() : TwitchMessageEvent {
    fun normalizeMessage(message: String) =
        Normalizer.normalize(message, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")

    fun messageContainsLink(message: String): Boolean {
        val couldBeATopLevelDomainEnding = """\S+\.\S{2,3}\b"""
        return message.contains("""http(s)?://""".toRegex()) ||
                message.contains(couldBeATopLevelDomainEnding.toRegex())
    }

    return TwitchMessageEvent(
        channel = this.channel.name,
        userInfo = UserInfo(
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
