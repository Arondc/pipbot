package de.arondc.pipbot.frontend.dtos

import de.arondc.pipbot.channels.ShoutoutOnRaidType
import java.util.*

class ChannelDTO(
    val id: Long? = null,
    val name: String = "",
    val language: Locale = Locale.GERMAN,
    val active: Boolean = true,
    val shoutOutOnRaidMode: ShoutoutOnRaidType = ShoutoutOnRaidType.NONE,
    val shoutoutChannels: List<String> = listOf()
)