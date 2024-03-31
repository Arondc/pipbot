package de.arondc.pipbot.frontend

import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.frontend.dtos.ChannelDTO
import de.arondc.pipbot.frontend.dtos.MemeDTO
import de.arondc.pipbot.frontend.dtos.StreamDTO
import de.arondc.pipbot.memes.MemeService
import de.arondc.pipbot.streams.StreamService
import de.arondc.pipbot.streams_merge.MergeService
import de.arondc.pipbot.twitch.TwitchStreamService
import mu.KotlinLogging
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
    val mergeService: MergeService
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
        val existingChannel = channelService.findByNameIgnoreCase(newChannel.name)
        if (existingChannel == null) {
            val channelEntity = conversionService.convert(newChannel, ChannelEntity::class.java)!!
            channelService.save(channelEntity)
            twitchStreamService.joinChannel(channelEntity.name)
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
            if (!channel.active) {
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

}

class FrontendException(message: String) : RuntimeException(message)