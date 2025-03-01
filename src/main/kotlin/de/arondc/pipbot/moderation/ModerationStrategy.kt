package de.arondc.pipbot.moderation

import de.arondc.pipbot.userchannelinformation.UserInformation
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

fun interface ModerationStrategy {
    fun calculateUserTrustLevel(userInformation: UserInformation) : UserTrustLevel
}

object ModerationByFollowAge : ModerationStrategy {
    override fun calculateUserTrustLevel(userInformation: UserInformation): UserTrustLevel {
        fun isFollower():Boolean = userInformation.followerSince != null
        fun followAgeInMonths(): Long = ChronoUnit.MONTHS.between(userInformation.followerSince, LocalDateTime.now())
        return when {
            isFollower() && followAgeInMonths() < 6 -> UserTrustLevel.NEW_FOLLOWER
            isFollower() && followAgeInMonths() < 12 -> UserTrustLevel.SHORT_TERM_FOLLOWER
            isFollower() && followAgeInMonths() >= 12 -> UserTrustLevel.LONG_TERM_FOLLOWER
            else -> UserTrustLevel.VIEWER
        }
    }
}

object ModerationByAttendance : ModerationStrategy {
    override fun calculateUserTrustLevel(userInformation: UserInformation): UserTrustLevel {
        return when {
            userInformation.amountOfVisitedStreams <= 2L -> UserTrustLevel.VIEWER
            userInformation.amountOfVisitedStreams < 10  -> UserTrustLevel.NEW_FOLLOWER
            userInformation.amountOfVisitedStreams < 15  -> UserTrustLevel.SHORT_TERM_FOLLOWER
            userInformation.amountOfVisitedStreams >= 15 -> UserTrustLevel.LONG_TERM_FOLLOWER
            else -> {UserTrustLevel.LONG_TERM_FOLLOWER} // Should never happen
        }
    }
}