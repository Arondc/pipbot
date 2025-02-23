package de.arondc.pipbot.frontend

import de.arondc.pipbot.automod.AutoModPhraseEntity
import de.arondc.pipbot.automod.AutoModService
import de.arondc.pipbot.autoresponder.AutoResponseEntity
import de.arondc.pipbot.autoresponder.AutoResponseService
import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.events.EventPublishingService
import de.arondc.pipbot.events.JoinTwitchChannelEvent
import de.arondc.pipbot.events.LeaveTwitchChannelEvent
import de.arondc.pipbot.events.UpdateUserListForChannelEvent
import de.arondc.pipbot.frontend.dtos.*
import de.arondc.pipbot.memes.MemeService
import de.arondc.pipbot.streams.StreamService
import de.arondc.pipbot.streams_merge.MergeService
import mu.KotlinLogging
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Service

@Service
class FrontendService(
    val memeService: MemeService,
    val channelService: ChannelService,
    val streamService: StreamService,
    val conversionService: ConversionService,
    val mergeService: MergeService,
    val autoResponseService: AutoResponseService,
    val autoModService: AutoModService,
    val eventPublisher: EventPublishingService
) {
    private val log = KotlinLogging.logger {}

    fun getMemes(streamId: Long? = null): List<MemeDTO> {
        val memes =
            if (streamId != null) {
                memeService.findByStreamId(streamId)
            } else {
                memeService.findAll()
            }

        return memes.mapNotNull { conversionService.convert(it, MemeDTO::class.java) }
    }

    fun getChannels(): List<ChannelDTO> {
        val channels = channelService.findAll()
        return channels.mapNotNull { conversionService.convert(it, ChannelDTO::class.java) }
    }

    fun createNewChannel(newChannel: ChannelDTO) {
        if (!channelService.channelExists(newChannel.name)) {
            val channelEntity = conversionService.convert(newChannel, ChannelEntity::class.java)!!
            channelService.save(channelEntity)
            eventPublisher.publishEvent(JoinTwitchChannelEvent(channelEntity.name))
            eventPublisher.publishEvent(UpdateUserListForChannelEvent(channelEntity.name))
        } else {
            log.info { "Channel ${newChannel.name} exists already" }
            throw FrontendException("Channel ${newChannel.name} exists already")
        }
    }

    fun updateChannel(newChannelInformation: ChannelDTO) {
        val existingChannel = channelService.findById(newChannelInformation.id!!)
        if (existingChannel != null) {
            val channelEntity = conversionService.convert(newChannelInformation, ChannelEntity::class.java)!!
            channelService.save(channelEntity)
            if (existingChannel.name != channelEntity.name) {
                eventPublisher.publishEvent(LeaveTwitchChannelEvent(existingChannel.name))
                eventPublisher.publishEvent(JoinTwitchChannelEvent(channelEntity.name))
            }
        } else {
            log.info { "Channel with id ${newChannelInformation.id} does not exist" }
            throw FrontendException("Channel with id ${newChannelInformation.id} does not exist")
        }
    }

    fun getChannel(channelId: Long): ChannelDTO {
        val channelEntity = channelService.findById(channelId)
        return if (channelEntity == null) {
            ChannelDTO()
        } else {
            ChannelDTO(
                channelId,
                channelEntity.name,
                channelEntity.language,
                channelEntity.active,
                channelEntity.shoutoutOnRaid,
                channelEntity.automatedShoutoutChannels
            )
        }
    }

    fun deleteChannel(id: Long) {
        val channel = channelService.findById(id)
        if (channel != null) {
            channelService.deleteChannel(channel)
            if (channel.active) {
                eventPublisher.publishEvent(LeaveTwitchChannelEvent(channel.name))
            }
        }
    }

    fun activateChannel(channelId: Long) {
        val channel = channelService.findById(channelId)
        if (channel != null) {
            channelService.setActiveById(channelId, true)
            eventPublisher.publishEvent(JoinTwitchChannelEvent(channel.name))
        }
    }

    fun deactivateChannel(channelId: Long) {
        val channel = channelService.findById(channelId)
        if (channel != null) {
            channelService.setActiveById(channelId, false)
            eventPublisher.publishEvent(LeaveTwitchChannelEvent(channel.name))
        }
    }

    fun getStreams(): List<StreamDTO> {
        return streamService.findAll().mapNotNull { conversionService.convert(it, StreamDTO::class.java) }.toList()
    }

    fun mergeStreams(streamIds: List<Long>) {
        mergeService.mergeStreams(streamIds)
    }

    fun getAutoResponses(): List<AutoResponseDTO> {
        return autoResponseService.findAll().mapNotNull { conversionService.convert(it, AutoResponseDTO::class.java) }.toList()
    }

    fun getAutoResponse(autoResponseId: Long): AutoResponseDTO {
        val autoResponseEntity : AutoResponseEntity = autoResponseService.findById(autoResponseId) ?: return AutoResponseDTO()
        return conversionService.convert(autoResponseEntity, AutoResponseDTO::class.java)!!
    }

    fun createAutoResponse(autoResponseInformation: AutoResponseDTO) {
        val newEntity = conversionService.convert(autoResponseInformation, AutoResponseEntity::class.java)!!
        //TODO Kanäle als dropdown
        //TODO Commands nicht doppelt erlauben für den gleichen Kanal
        //TODO AutoResponses auch per command pflegbar machen
        if(newEntity.channel == null){
            throw RuntimeException("Kanal existiert nicht")
        }
        autoResponseService.save(newEntity)
    }

    fun updateAutoResponse(autoResponseInformation: AutoResponseDTO) {
        val existingAutoResponse = autoResponseService.findById(autoResponseInformation.id!!)
        if (existingAutoResponse != null) {
            val updatedAutoResponse = conversionService.convert(autoResponseInformation, AutoResponseEntity::class.java)!!
            autoResponseService.save(updatedAutoResponse)
        }
    }

    fun deleteAutoResponse(autoResponseId: Long) {
        val existingAutoResponse = autoResponseService.findById(autoResponseId)
        if(existingAutoResponse != null) {
            autoResponseService.delete(existingAutoResponse)
        }
    }

    fun getAutoModPhrases(): List<AutoModPhraseDTO> =
        autoModService.findAll().mapNotNull { conversionService.convert(it, AutoModPhraseDTO::class.java) }.toList()

    fun createAutoModPhrase(autoResponseInformation: AutoModPhraseDTO) {
        val newEntity = conversionService.convert(autoResponseInformation, AutoModPhraseEntity::class.java)!!
        autoModService.save(newEntity)
    }

    fun deleteAutoModPhrase(autoModPhraseId: Long) {
        val existingAutoModPhrase = autoModService.findById(autoModPhraseId)
        if (existingAutoModPhrase != null) {
            autoModService.delete(existingAutoModPhrase)
        }
    }

    fun getAutoModChannels(): List<AutoModChannelDTO> {
        return channelService.findAll().mapNotNull { conversionService.convert(it, AutoModChannelDTO::class.java) }
            .toList()
    }
}

class FrontendException(message: String) : RuntimeException(message)