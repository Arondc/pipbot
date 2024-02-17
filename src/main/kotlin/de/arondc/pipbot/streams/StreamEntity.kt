package de.arondc.pipbot.streams

import de.arondc.pipbot.channels.ChannelEntity
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
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
import java.time.LocalDateTime

@Repository
interface StreamRepository : JpaRepository<StreamEntity, Long> {

    fun findByChannelAndStartTimesContains(channel: ChannelEntity, startTime: LocalDateTime): StreamEntity?
    /*
    @Query("select stream from StreamEntity stream  where stream.channel = :channel and :startTime member of stream.startTimes")
    fun findByChannelAndStartTimesContains(
        @Param("channel") channel: ChannelEntity,
        @Param("startTime") startTime: LocalDateTime
    ): StreamEntity?*/
    //fun findByChannelAndStart(channel: ChannelEntity, start: Instant): StreamEntity?
}

@Entity
@Table(name = "streams")
class StreamEntity(

    @ElementCollection
    @CollectionTable(name = "streams_start_times")
    @Column(name = "start_time")
    val startTimes: Set<LocalDateTime>,
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


    //TODO: Streamtitel und Spiel auch merken?
    /*
    val startDate: String

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
    }*/
}