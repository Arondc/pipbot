package de.arondc.pipbot.events

data class TwitchMessageEvent(
    val channel: String,
    val userInfo: EventUserInfo,
    val messageInfo: EventMessageInfo,

    )

data class EventUserInfo(
    val userName: String,
    val permissions: Set<TwitchPermission>
)

data class EventMessageInfo(
    val text: String,
    val normalizedText: String,
)


data class SendMessageEvent(val channel: String, val message: String)
data class TwitchRaidEvent(val raidedChannel: String, val incomingRaider: String, val size: Int)
data class JoinTwitchChannelEvent(val channel: String)
data class LeaveTwitchChannelEvent(val channel: String)
data class UpdateUserListForChannelEvent(val channel: String)
data class UpdateChannelInformationForUserEvent(val channel: String, val user: String, val permissions: Set<TwitchPermission> = emptySet())
data class NewAutoModPhraseEvent(val channel: String, val newPhrase: String)
data class ModerationActionEvent(val channel: String, val user: String)