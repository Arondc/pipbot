package de.arondc.pipbot.twitch

enum class TwitchScope(val scopeName: String) {
    USER_BOT("user:bot"),
    CHAT_READ("chat:read"),
    CHAT_EDIT("chat:edit"),
    MODERATOR_READ_FOLLOWERS("moderator:read:followers"),
    MODERATOR_READ_CHATTERS("moderator:read:chatters"),
    MODERATOR_MANAGE_SHOUTOUTS("moderator:manage:shoutouts"),
    MODERATOR_MANAGE_BANNED_USERS("moderator:manage:banned_users"),
    WHISPERS_READ("whispers:read"),
    WHISPERS_EDIT("whispers:edit"),
    USER_MANAGE_WHISPERS("user:manage:whispers"),
    USER_READ_FOLLOWS("user:read:follows")
}