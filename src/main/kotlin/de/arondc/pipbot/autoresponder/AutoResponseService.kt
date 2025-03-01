package de.arondc.pipbot.autoresponder

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class AutoResponseService(private val autoResponseRepository: AutoResponseRepository) {
    fun findAll(): List<AutoResponseEntity> {
        return autoResponseRepository.findAll()
    }

    fun findById(autoResponseId: Long): AutoResponseEntity? {
        return autoResponseRepository.findByIdOrNull(autoResponseId)
    }

    fun save(newEntity: AutoResponseEntity) {
        autoResponseRepository.save(newEntity)
    }

    fun delete(autoResponse: AutoResponseEntity) {
        autoResponseRepository.delete(autoResponse)
    }

    fun getAutoResponse(channel: String, command: String): String? {
        return autoResponseRepository.findByChannelNameAndCommand(channel, command)?.message
    }
}