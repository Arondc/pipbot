package de.arondc.pipbot.frontend.converters

import de.arondc.pipbot.channels.ShoutoutOnRaidType
import org.springframework.format.Formatter
import java.util.Locale

class ShoutoutOnRaidTypeFormatter : Formatter<ShoutoutOnRaidType> {
    override fun print(sor: ShoutoutOnRaidType, locale: Locale): String {
        return when(sor) {
            ShoutoutOnRaidType.NONE -> "Aus"
            ShoutoutOnRaidType.TEXT -> "Textantwort"
            ShoutoutOnRaidType.STREAM_ELEMENTS_SHOUTOUT -> "Stream Elements shoutout"
            ShoutoutOnRaidType.TWITCH_SHOUTOUT -> "Twitch shoutout"
        }
    }

    override fun parse(text: String, locale: Locale): ShoutoutOnRaidType {
        return when(text) {
            "Aus" -> ShoutoutOnRaidType.NONE
            "Textantwort" -> ShoutoutOnRaidType.TEXT
            "Stream Elements shoutout" -> ShoutoutOnRaidType.STREAM_ELEMENTS_SHOUTOUT
            "Twitch shoutout" -> ShoutoutOnRaidType.TWITCH_SHOUTOUT
            else -> ShoutoutOnRaidType.NONE
        }
    }

}