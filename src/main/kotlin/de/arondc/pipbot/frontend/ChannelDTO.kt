package de.arondc.pipbot.frontend

import de.arondc.pipbot.channels.ShoutoutOnRaidType
import java.util.Locale

class ChannelDTO(
    val id : Long? = null,
    val name : String = "",
    val language : Locale = Locale.GERMAN,
    val shoutOutOnRaidMode : ShoutoutOnRaidType = ShoutoutOnRaidType.NONE,
    val shoutoutChannels : List<String> = listOf()
)