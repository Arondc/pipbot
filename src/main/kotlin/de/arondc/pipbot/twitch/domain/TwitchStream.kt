package de.arondc.pipbot.twitch.domain

import java.time.LocalDateTime

data class TwitchStream(val userName: String, val userLogin: String, val startingTime: LocalDateTime)