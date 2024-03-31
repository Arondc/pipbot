package de.arondc.pipbot.events

import de.arondc.pipbot.twitch.TwitchPermission

data class TwitchMessage(val channel: String, val user: String, val message: String, val permissions: Set<TwitchPermission>)
data class SendMessageEvent(val channel: String, val message: String)
data class TwitchRaidEvent(val raidedChannel: String, val incomingRaider: String, val size: Int)
data class JoinTwitchChannelEvent(val channel: String)
data class LeaveTwitchChannelEvent(val channel: String)