package de.arondc.pipbot.frontend

import de.arondc.pipbot.autoresponder.AutoResponseEntity
import de.arondc.pipbot.autoresponder.AutoResponseService
import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.events.UpdateUserListForChannelEvent
import de.arondc.pipbot.frontend.dtos.AutoResponseDTO
import de.arondc.pipbot.frontend.dtos.ChannelDTO
import de.arondc.pipbot.frontend.dtos.MemeDTO
import de.arondc.pipbot.frontend.dtos.StreamDTO
import de.arondc.pipbot.memes.MemeService
import de.arondc.pipbot.streams.StreamService
import de.arondc.pipbot.streams_merge.MergeService
import de.arondc.pipbot.twitch.TwitchStreamService
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FrontendService(
    val memeService: MemeService,
    val channelService: ChannelService,
    val streamService: StreamService,
    val conversionService: ConversionService,
    val twitchStreamService: TwitchStreamService,
    val mergeService: MergeService,
    val autoResponseService: AutoResponseService,
    val eventPublisher: ApplicationEventPublisher
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

    @Transactional
    fun createNewChannel(newChannel: ChannelDTO) {
        val existingChannel = channelService.findByNameIgnoreCase(newChannel.name)
        if (existingChannel == null) {
            val channelEntity = conversionService.convert(newChannel, ChannelEntity::class.java)!!
            channelService.save(channelEntity)
            twitchStreamService.joinChannel(channelEntity.name)
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
                twitchStreamService.leaveChannel(existingChannel.name)
                twitchStreamService.joinChannel(channelEntity.name)
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
                twitchStreamService.leaveChannel(channel.name)
            }
        }
    }

    @Transactional
    fun activateChannel(channelId: Long) {
        val channel = channelService.findById(channelId)
        if (channel != null) {
            channelService.setActiveById(channelId, true)
            twitchStreamService.joinChannel(channel.name)
        }
    }

    fun deactivateChannel(channelId: Long) {
        val channel = channelService.findById(channelId)
        if (channel != null) {
            channelService.setActiveById(channelId, false)
            twitchStreamService.leaveChannel(channel.name)
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

}

class FrontendException(message: String) : RuntimeException(message)