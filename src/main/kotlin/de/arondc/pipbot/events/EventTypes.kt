package de.arondc.pipbot.events

sealed interface PipBotEvent


data class MessageEvent(
    val channel: String,
    val userInfo: TwitchUserInfo,
    val messageInfo: MessageInfo,
) : PipBotEvent

data class MessageInfo(val text: String, val normalizedText: String, val hasLink: Boolean)

data class TwitchUserInfo(
    val userName: String,
    val permissions: Set<TwitchPermission>,
    val subscriberMonths: Int,
    val subscriptionTier: Int
)

data class AutoModPreCheckEvent(
    val channel: String,
    val messageInfo: MessageInfo,
    val userInfo: TwitchUserInfo,
) : PipBotEvent

data class ProcessingEvent(
    val channel: String,
    val messageInfo: MessageInfo,
    val userInfo: TwitchUserInfo,
) : PipBotEvent


data class TwitchRaidEvent(val raidedChannel: String, val incomingRaider: String, val size: Int) : PipBotEvent
data class TwitchCallEvent(val callType: CallType, val channel: String, val message : String = "") : PipBotEvent
enum class CallType {SEND_MESSAGE,JOIN_CHANNEL,LEAVE_CHANNEL}

data class NewAutoModPhraseEvent(val channel: String, val newPhrase: String) : PipBotEvent
data class ModerationActionEvent(val channel: String, val user: String) : PipBotEvent