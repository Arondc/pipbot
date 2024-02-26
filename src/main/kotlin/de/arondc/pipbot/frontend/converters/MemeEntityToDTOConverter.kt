package de.arondc.pipbot.frontend.converters

import de.arondc.pipbot.frontend.MemeDTO
import de.arondc.pipbot.memes.MemeEntity
import org.springframework.core.convert.converter.Converter
import java.time.LocalDateTime
import java.util.regex.Pattern


class MemeEntityToDTOConverter : Converter<MemeEntity, MemeDTO> {
    override fun convert(entity: MemeEntity): MemeDTO {
        return MemeDTO(
            entity.channel!!.name,
            entity.sentByUser,
            entity.recordedAt,
            extractLink(entity),
            entity.message,
            entity.stream?.id,
            getFirstStartOfStream(entity)
        )
    }

    private fun getFirstStartOfStream(memeEntity: MemeEntity): LocalDateTime? {
        return try {
            memeEntity.stream!!.startTimes.stream().sorted().findFirst().get()
        } catch (npe: NullPointerException) {
            null
        }
    }

    private fun extractLink(memeEntity: MemeEntity): String {
        return extractLink(memeEntity.message)
    }

    private fun extractLink(message: String): String {
        val p = Pattern.compile("https?:(.*?)(\\s.*|$)")
        val m = p.matcher(message)
        return if (m.find()) m.group(1) else ""
    }
}

