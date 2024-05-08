package de.arondc.pipbot.frontend.dtos

class AutoResponseDTO(
    val id: Long? = null,
    val channel: String = "",
    val command: String = "",
    val message: String = "",
)