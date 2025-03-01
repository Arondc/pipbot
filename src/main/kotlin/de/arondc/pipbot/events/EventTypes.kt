package de.arondc.pipbot.events

sealed interface PipBotEvent

data class TwitchMessageEvent(
    val channel: String,
    val userInfo: UserInfo,
    val messageInfo: MessageInfo,
) :
    PipBotEvent {
    data class UserInfo(
        val userName: String,
        val permissions: Set<TwitchPermission>,
        val subscriberMonths: Int,
        val subscriptionTier: Int
    )

    data class MessageInfo(val text: String, val normalizedText: String, val hasLink: Boolean)
}

data class SendMessageEvent(val channel: String, val message: String) : PipBotEvent
data class TwitchRaidEvent(val raidedChannel: String, val incomingRaider: String, val size: Int) : PipBotEvent
data class JoinTwitchChannelEvent(val channel: String) : PipBotEvent
data class LeaveTwitchChannelEvent(val channel: String) : PipBotEvent
data class UpdateChannelInformationForUserEvent(
    val channel: String, val user: String, val permissions: Set<TwitchPermission> = emptySet()
) : PipBotEvent

data class NewAutoModPhraseEvent(val channel: String, val newPhrase: String) : PipBotEvent
data class ModerationActionEvent(val channel: String, val user: String) : PipBotEvent