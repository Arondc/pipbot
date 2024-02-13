package de.arondc.pipbot.streams

import de.arondc.pipbot.channels.ChannelEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToOne
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Repository
interface StreamRepository : JpaRepository<StreamEntity, Long> {
    fun findByChannelAndStart(channel: ChannelEntity, start: Instant) : StreamEntity?
}

@Entity
@Table(name = "streams")
class StreamEntity(
    val start: Instant? = null,
    @ManyToOne
    @JoinColumn(nullable = false)
    private val channel: ChannelEntity,
    @OneToOne
    private val mergedTo: StreamEntity? = null, //TODO Zusammenführung anders umsetzen
    @Id
    @SequenceGenerator(name = "streams_sequence", sequenceName = "STREAMS_SEQ", allocationSize = 1)
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "streams_sequence"
    )
    val id: Long? = null
) {
    val startDate: String
        //TODO: Streamtitel und Spiel auch merken?
        get() {
            if (start == null) {
                //classic dev comment incoming -> This should never happen!
                return "Kein Datum ermittelbar"
            }
            return DD_MM_YYYY.format(start.truncatedTo(ChronoUnit.DAYS))
        }

    companion object {
        private val DD_MM_YYYY: DateTimeFormatter = DateTimeFormatter.ofPattern(
            "dd.MM.yyyy"
        )
            .withZone(ZoneId.of("Europe/Berlin"))
    }
}