package de.arondc.pipbot.streams

import de.arondc.pipbot.channels.ChannelEntity
import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface StreamRepository : JpaRepository<StreamEntity, Long> {
    fun findByChannelAndStartTimesContains(channel: ChannelEntity, startTime: LocalDateTime): StreamEntity?
    fun findAllByChannelName(channelName: String): List<StreamEntity>

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
) {

    fun associateAdditionalStartTime(additionalStartTime: Set<LocalDateTime>): StreamEntity {
        return StreamEntity(startTimes.toMutableSet().plus(additionalStartTime), channel, id)
    }

}