package de.arondc.pipbot.channels

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository

interface ChannelRepository : JpaRepository<ChannelEntity, Long> {
    fun findByName(name: String): ChannelEntity?
}

@Entity
@Table(name = "channels")
class ChannelEntity(
    val name: String,
    @Id @SequenceGenerator(
        name = "channels_sequence",
        sequenceName = "CHANNELS_SEQ",
        allocationSize = 1
    ) @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "channels_sequence") var id: Long? = null
)