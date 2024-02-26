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
import jakarta.persistence.SequenceGenerator
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface StreamRepository : JpaRepository<StreamEntity, Long> {
    fun findByChannelAndStartTimesContains(channel: ChannelEntity, startTime: LocalDateTime): StreamEntity?
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
    val channel: ChannelEntity,
    @Id
    @SequenceGenerator(name = "streams_sequence", sequenceName = "STREAMS_SEQ", allocationSize = 1)
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "streams_sequence"
    )
    val id: Long? = null
){

    fun associateAdditionalStartTime(additionalStartTime : Set<LocalDateTime>) : StreamEntity {
        return StreamEntity(startTimes.toMutableSet().plus(additionalStartTime), channel, id)
    }

}