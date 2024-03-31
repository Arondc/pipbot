package de.arondc.pipbot.quotes

import de.arondc.pipbot.channels.ChannelEntity
import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface QuoteRepository : JpaRepository<QuoteEntity, Long> {
    @Query("select max(q.number) from QuoteEntity q where q.channel = :channel")
    fun findMaxNumberByChannel(channel: ChannelEntity): Long?

    fun findByNumberAndChannel(number: Long, channel: ChannelEntity): QuoteEntity?

    fun findByTextContainingIgnoreCaseAndChannel(text: String, channel: ChannelEntity): List<QuoteEntity>
}

@Entity
@Table(name = "quotes")
class QuoteEntity(
    val text: String,
    val number: Long,
    @ManyToOne
    val channel: ChannelEntity? = null,
    @Id @SequenceGenerator(name = "quotes_sequence", sequenceName = "QUOTES_SEQ", allocationSize = 1) @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "quotes_sequence"
    ) val id: Long? = null
)