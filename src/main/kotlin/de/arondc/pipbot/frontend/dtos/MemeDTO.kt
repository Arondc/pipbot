package de.arondc.pipbot.frontend.dtos

import java.time.LocalDateTime

class MemeDTO(
    val channelName: String,
    val sentByUser: String,
    val recordedAt: LocalDateTime,
    val link: String,
    val message: String,
    val streamId: Long?,
    val streamStart: LocalDateTime?
)