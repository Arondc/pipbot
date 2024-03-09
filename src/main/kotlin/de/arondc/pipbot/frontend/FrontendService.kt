package de.arondc.pipbot.frontend

import de.arondc.pipbot.channels.ChannelEntity
import de.arondc.pipbot.channels.ChannelService
import de.arondc.pipbot.memes.MemeService
import de.arondc.pipbot.twitch.BotTwitchConnector
import mu.KotlinLogging
import org.springframework.core.convert.ConversionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FrontendService(val memeService: MemeService, val channelService: ChannelService, val conversionService: ConversionService, val botTwitchConnector: BotTwitchConnector) {
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

    fun getChannels() : List<ChannelDTO> {
        val channels = channelService.findAll()
        return channels.mapNotNull { conversionService.convert(it, ChannelDTO::class.java) }
    }

    fun createNewChannel(newChannel: ChannelDTO) {
        val existingChannel = channelService.findByNameIgnoreCase(newChannel.name)
        if (existingChannel == null) {
            val channelEntity = conversionService.convert(newChannel, ChannelEntity::class.java)!!
            channelService.save(channelEntity)
            botTwitchConnector.joinTwitchChannel(channelEntity.name)
        } else {
            log.info{"Channel ${newChannel.name} exists already"}
            throw FrontendException("Channel ${newChannel.name} exists already")
        }
    }

    fun updateChannel(newChannelInformation : ChannelDTO) {
        val existingChannel = channelService.findById(newChannelInformation.id!!)
        if(existingChannel != null){
            val channelEntity = conversionService.convert(newChannelInformation, ChannelEntity::class.java)!!
            channelService.save(channelEntity)
            if(existingChannel.name != channelEntity.name) {
                botTwitchConnector.leaveTwitchChannel(existingChannel.name)
                botTwitchConnector.joinTwitchChannel(channelEntity.name)
            }
        } else {
            log.info{"Channel with id ${newChannelInformation.id} does not exist"}
            throw FrontendException("Channel with id ${newChannelInformation.id} does not exist")
        }
    }

    fun getChannel(channelId: Long): ChannelDTO {
        val channelEntity = channelService.findById(channelId)
        return if(channelEntity == null){
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
        if(channel != null){
            channelService.deleteChannel(channel)
            if(!channel.active) {
                botTwitchConnector.leaveTwitchChannel(channel.name)
            }
        }
    }

    @Transactional
    fun activateChannel(channelId: Long){
        val channel = channelService.findById(channelId)
        if(channel != null) {
            channelService.setActiveById(channelId, true)
            botTwitchConnector.joinTwitchChannel(channel.name)
        }
    }

    fun deactivateChannel(channelId : Long) {
        val channel = channelService.findById(channelId)
        if(channel != null) {
            channelService.setActiveById(channelId, false)
            botTwitchConnector.leaveTwitchChannel(channel.name)
        }
    }
}

class FrontendException(message: String) : RuntimeException(message)