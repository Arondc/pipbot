package de.arondc.pipbot.twitch

data class TwitchMessage(val channel: String, val user: String, val message: String, val permissions: Set<String>)
data class SendMessageEvent(val channel: String, val message: String)
