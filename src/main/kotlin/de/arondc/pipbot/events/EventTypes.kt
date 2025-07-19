package de.arondc.pipbot.events

sealed interface PipBotEvent

//Incoming

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

//Processing
data class ProcessingEvent(
    val channel: String,
    val messageInfo: TwitchMessageEvent.MessageInfo,
    val userInfo: TwitchMessageEvent.UserInfo
) : PipBotEvent

//ExternalCall


data class TwitchRaidEvent(val raidedChannel: String, val incomingRaider: String, val size: Int) : PipBotEvent

data class TwitchCallEvent(val callType: CallType, val channel: String, val message : String = "") : PipBotEvent
enum class CallType {SEND_MESSAGE,JOIN_CHANNEL,LEAVE_CHANNEL}

data class NewAutoModPhraseEvent(val channel: String, val newPhrase: String) : PipBotEvent
data class ModerationActionEvent(val channel: String, val user: String) : PipBotEvent