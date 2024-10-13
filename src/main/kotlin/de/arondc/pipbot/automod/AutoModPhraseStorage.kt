package de.arondc.pipbot.automod

import de.arondc.pipbot.channels.ChannelEntity
import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AutoModPhraseRepository : JpaRepository<AutoModPhraseEntity, Long>{
    fun findByChannelName(channel: String): List<AutoModPhraseEntity>
}

@Entity
@Table(name = "auto_mod_phrase")
class AutoModPhraseEntity(
    val text: String,
    @ManyToOne
    val channel: ChannelEntity? = null,
    @Id @SequenceGenerator(
        name = "auto_mod_phrase_sequence",
        sequenceName = "AUTO_MOD_PHRASE_SEQ",
        allocationSize = 1
    ) @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "auto_mod_phrase_sequence"
    ) val id: Long? = null
)