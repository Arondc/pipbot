package de.arondc.pipbot.channels

import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import java.util.*

interface ChannelRepository : JpaRepository<ChannelEntity, Long> {
    fun findByNameIgnoreCase(name: String): ChannelEntity
    fun findAllByActiveIsTrue(): List<ChannelEntity>
    fun existsByNameIgnoreCase(name: String): Boolean

    @Transactional
    @Modifying
    @Query("update ChannelEntity channel set channel.active = :active where channel.id = :id")
    fun setActiveById(@Param("id") id: Long, @Param("active") active: Boolean)

}

@Entity
@Table(name = "channels")
class ChannelEntity(
    val name: String,
    val language: Locale,
    @Enumerated(EnumType.STRING)
    val shoutoutOnRaid: ShoutoutOnRaidType,
    @ElementCollection
    @CollectionTable(name = "channels_automated_shoutouts")
    @Column(name = "channel_name")
    val automatedShoutoutChannels: List<String>,
    val active: Boolean = true,
    @Id @SequenceGenerator(
        name = "channels_sequence",
        sequenceName = "CHANNELS_SEQ",
        allocationSize = 1
    ) @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "channels_sequence") var id: Long? = null
)

enum class ShoutoutOnRaidType {
    NONE, TEXT, STREAM_ELEMENTS_SHOUTOUT, TWITCH_SHOUTOUT
}
