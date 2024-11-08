package de.arondc.pipbot.moderation

import de.arondc.pipbot.channels.ChannelEntity
import jakarta.persistence.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ModerationResponseStorage : JpaRepository<ModerationResponseEntity, Long> {
    fun findByChannelAndTrustLevel(channel: ChannelEntity, trustLevel: UserTrustLevel): ModerationResponseEntity?
}

@Entity
@Table(name = "auto_mod_configuration_moderation_response")
class ModerationResponseEntity(
    @ManyToOne
    val channel: ChannelEntity,
    @Enumerated(EnumType.STRING)
    val trustLevel: UserTrustLevel,
    @Enumerated(EnumType.STRING)
    val type : ModerationResponeType,
    val duration: Long? = null,
    val text: String? = null,
    @Id @SequenceGenerator(
        name = "auto_mod_configuration_moderation_response_sequence",
        sequenceName = "AUTO_MOD_CONFIGURATION_MODERATION_RESPONSE_SEQ",
        allocationSize = 1
    ) @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "auto_mod_configuration_moderation_response_sequence"
    ) val id: Long? = null
)

enum class ModerationResponeType {
    BAN,TIMEOUT,TEXT
}