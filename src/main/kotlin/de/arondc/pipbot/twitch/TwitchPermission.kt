package de.arondc.pipbot.twitch

enum class TwitchPermission(private val level : Long) {
    //ROLES WITHOUT SPECIAL PERMISSIONS
    PRIME_TURBO(0),
    PARTNER(0),
    SUBGIFTER(0),
    FOUNDER(0),
    BITS_CHEERER(0),
    FORMER_HYPE_TRAIN_CONDUCTOR(0),
    CURRENT_HYPE_TRAIN_CONDUCTOR(0),
    PREDICTIONS_BLUE(0),
    PREDICTIONS_PINK(0),
    NO_AUDIO(0),
    NO_VIDEO(0),
    MOMENTS(0),
    ARTIST(0),
    VIP(0),
    TWITCHSTAFF(0),

    //ROLES WITH SPECIAL PERMISSION
    EVERYONE(0),
    SUBSCRIBER(10),
    MODERATOR(20),
    BROADCASTER(30),
    OWNER(100);

    fun isSatisfiedBy(permission : TwitchPermission) : Boolean {
        return permission.level >= this.level
    }
}

fun Iterable<TwitchPermission>.satisfies(minimalPermission : TwitchPermission) : Boolean {
    return this.any { minimalPermission.isSatisfiedBy(it) }
}