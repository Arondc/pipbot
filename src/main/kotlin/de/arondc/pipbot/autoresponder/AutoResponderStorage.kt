package de.arondc.pipbot.autoresponder

import de.arondc.pipbot.channels.ChannelEntity
import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AutoResponseRepository : JpaRepository<AutoResponseEntity, Long> {
    fun findByChannelNameAndCommand(channelName: String, command: String): AutoResponseEntity?
}

@Entity
@Table(name = "auto_response")
class AutoResponseEntity(
    @ManyToOne val channel: ChannelEntity? = null,
    val command: String,
    val message: String,
    @Id @SequenceGenerator(name = "auto_response_sequence", sequenceName = "AUTO_RESPONSE_SEQ", allocationSize = 1) @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "auto_response_sequence"
    ) var id: Long? = null
)