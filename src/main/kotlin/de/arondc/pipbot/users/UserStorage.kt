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
    fun findByName(username: String): UserEntity?
}

@Repository
interface UserChannelInformationStorage : JpaRepository<UserChannelInformationEntity, Long> {
    fun findByUserNameIgnoreCaseAndChannelNameIgnoreCase(username: String, channelName: String): UserChannelInformationEntity?
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
class UserChannelInformationEntity(
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    val user : UserEntity,

    @ManyToOne
    @MapsId("channelId")
    @JoinColumn(name = "channel_id")
    val channel: ChannelEntity,

    val lastSeen: LocalDateTime? = null,

    val amountOfVisitedStreams: Long = 0,

    @Enumerated(EnumType.STRING)
    val highestTwitchUserLevel: TwitchPermission = TwitchPermission.EVERYONE,

    @EmbeddedId
    val id : UserChannelInformationEntityPK = UserChannelInformationEntityPK(user.id!!, channel.id!!)
){
    fun withNewLastSeenAndCount() = UserChannelInformationEntity(
        user = user,
        channel = channel,
        lastSeen = LocalDateTime.now(),
        amountOfVisitedStreams = amountOfVisitedStreams + 1,
        highestTwitchUserLevel = highestTwitchUserLevel,
        id = id
    )

    fun withNewLastSeen() = UserChannelInformationEntity(
        user = user,
        channel = channel,
        lastSeen = LocalDateTime.now(),
        amountOfVisitedStreams = amountOfVisitedStreams,
        highestTwitchUserLevel = highestTwitchUserLevel,
        id = id
    )

    fun withHighestUserLevel(highestTwitchUserLevel: TwitchPermission) = UserChannelInformationEntity(
        user = user,
        channel = channel,
        lastSeen = lastSeen,
        amountOfVisitedStreams = amountOfVisitedStreams,
        highestTwitchUserLevel = highestTwitchUserLevel,
        id = id
    )

}