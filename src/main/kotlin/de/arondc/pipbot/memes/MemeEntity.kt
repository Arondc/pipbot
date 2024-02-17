package de.arondc.pipbot.memes

import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.streams.StreamEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface MemeRepository : JpaRepository<MemeEntity, Long>

@Entity
@Table(name = "memes")
class MemeEntity(
    val recordedAt: LocalDateTime,
    @ManyToOne val channel: ChannelEntity? = null,
    val sentByUser: String,
    val message: String,
    val link: String, //TODO Brauchen wir das wirklich als eigenes Datenbankfeld?
    @ManyToOne val stream: StreamEntity? = null,
    @Id @SequenceGenerator(name = "memes_sequence", sequenceName = "MEMES_SEQ", allocationSize = 1) @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "memes_sequence"
    ) var id: Long? = null
) {
    override fun toString(): String {
        return "$recordedAt - $channel - $sentByUser - $message - $link - ${stream?.id}"
    }
}