package de.arondc.pipbot.userchannelinformation

import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.events.TwitchPermission
import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface UserChannelInformationStorage : JpaRepository<UserInformation, Long> {
    fun findByUserNameIgnoreCaseAndChannelNameIgnoreCase(username: String, channelName: String): UserInformation?
}

@Entity
@IdClass(UserChannelInformationEntityPK::class)
@Table(name = "user_channel_information")
class UserInformation(
    @Id
    @ManyToOne
    @JoinColumn(name = "channel_id")
    val channel: ChannelEntity,

    @Id
    val userName : String,
    var lastSeen: LocalDateTime = LocalDateTime.now(),

    var amountOfVisitedStreams: Long = 0,

    @Enumerated(EnumType.STRING)
    var highestTwitchUserLevel: TwitchPermission = TwitchPermission.EVERYONE,

    var followerSince: LocalDateTime? = null,
    var followerVerifiedOnce: Boolean = false,
)

@Embeddable
class UserChannelInformationEntityPK(
    val userName : String,
    val channel: Long,
)