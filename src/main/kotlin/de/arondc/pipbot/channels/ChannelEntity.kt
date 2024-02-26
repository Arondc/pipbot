package de.arondc.pipbot.channels

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Locale

interface ChannelRepository : JpaRepository<ChannelEntity, Long> {
    fun findByName(name: String): ChannelEntity?
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
    @Id @SequenceGenerator(
        name = "channels_sequence",
        sequenceName = "CHANNELS_SEQ",
        allocationSize = 1
    ) @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "channels_sequence") var id: Long? = null
)

enum class ShoutoutOnRaidType {
    NONE, TEXT, STREAM_ELEMENTS_SHOUTOUT, TWITCH_SHOUTOUT
}
