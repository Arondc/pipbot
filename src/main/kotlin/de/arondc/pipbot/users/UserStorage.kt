package de.arondc.pipbot.users

import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.events.TwitchPermission
import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.io.Serializable
import java.time.LocalDateTime

@Repository
interface UserStorage : JpaRepository<UserEntity, Long>{
    fun findByNameIgnoreCase(username: String): UserEntity?
}

@Repository
interface UserChannelInformationStorage : JpaRepository<UserInformation, Long> {
    fun findByUserNameIgnoreCaseAndChannelNameIgnoreCase(username: String, channelName: String): UserInformation?
}

@Entity
@Table(name = "users")
class UserEntity(
    val name: String,
    @Id @SequenceGenerator(name = "users_sequence", sequenceName = "USERS_SEQ", allocationSize = 1) @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "users_sequence"
    ) var id: Long? = null
)

@Embeddable
class UserChannelInformationEntityPK(
    val userId : Long,
    val channelId: Long,
) : Serializable

@Entity
@Table(name = "user_channel_information")
class UserInformation(
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    val user: UserEntity,

    @ManyToOne
    @MapsId("channelId")
    @JoinColumn(name = "channel_id")
    val channel: ChannelEntity,

    var lastSeen: LocalDateTime = LocalDateTime.now(),

    var amountOfVisitedStreams: Long = 0,

    @Enumerated(EnumType.STRING)
    var highestTwitchUserLevel: TwitchPermission = TwitchPermission.EVERYONE,

    var followerSince: LocalDateTime? = null,

    @EmbeddedId
    val id: UserChannelInformationEntityPK = UserChannelInformationEntityPK(user.id!!, channel.id!!)
)