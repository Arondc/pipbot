package de.arondc.pipbot.frontend.dtos

import java.time.LocalDateTime

class StreamDTO(val id: Long, val channelName: String, val startTimes: List<LocalDateTime>)