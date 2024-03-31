package de.arondc.pipbot.memes

import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.streams.StreamEntity
import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface MemeRepository : JpaRepository<MemeEntity, Long> {
    fun findByStream(stream: StreamEntity): Set<MemeEntity>
    fun findByStreamId(id: Long): List<MemeEntity>
}

@Entity
@Table(name = "memes")
class MemeEntity(
    val recordedAt: LocalDateTime,
    @ManyToOne val channel: ChannelEntity? = null,
    val sentByUser: String,
    val message: String,
    @ManyToOne val stream: StreamEntity? = null,
    @Id @SequenceGenerator(name = "memes_sequence", sequenceName = "MEMES_SEQ", allocationSize = 1) @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "memes_sequence"
    ) var id: Long? = null
) {
    override fun toString(): String {
        return "$recordedAt - $channel - $sentByUser - $message - ${stream?.id}"
    }

    fun associateToNewStream(newStream: StreamEntity): MemeEntity {
        return MemeEntity(recordedAt, channel, sentByUser, message, newStream, id)
    }
}