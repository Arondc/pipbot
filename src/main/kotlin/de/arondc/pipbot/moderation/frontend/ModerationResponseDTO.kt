package de.arondc.pipbot.moderation.frontend


class ModerationResponseDTO(
    val id: Long? = null,
    val channel: String = "",
    val trustLevel: String = "",
    val type: String = "",
    val text: String = "",
    val duration: String = ""
)