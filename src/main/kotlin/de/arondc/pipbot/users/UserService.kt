package de.arondc.pipbot.users

import de.arondc.pipbot.channels.ChannelRepository
import de.arondc.pipbot.streams.StreamEntity
import de.arondc.pipbot.streams.StreamService
import org.springframework.stereotype.Service

@Service
class UserService(
    val userStorage: UserStorage,
    val channelRepository: ChannelRepository,
    val userChannelInformationStorage: UserChannelInformationStorage,
    val streamService: StreamService
) {

    fun getUserChannelInformation(userName: String, channelName: String): UserChannelInformationEntity? =
        userChannelInformationStorage.findByUserNameIgnoreCaseAndChannelNameIgnoreCase(userName, channelName)


    fun recognizeUser(channelName: String, userName: String) {
        val user = userStorage.findByName(userName) ?: userStorage.save(UserEntity(userName))

        val channel = channelRepository.findByNameIgnoreCase(channelName)!!
        val info = userChannelInformationStorage.findByUserNameIgnoreCaseAndChannelNameIgnoreCase(userName, channelName)
            ?: userChannelInformationStorage.save(UserChannelInformationEntity(user, channel))

        val stream = streamService.findCurrentStream(channelName)

        updateLastSeenOfUser(info, stream)
    }

    private fun updateLastSeenOfUser(
        info: UserChannelInformationEntity,
        stream: StreamEntity?
    ) {
        val lastSeen = info.lastSeen
        if (stream != null && (lastSeen == null || lastSeen.isBefore(stream.startTimes.min()))) {
            //Stream ist online und Nutzer neu oder noch nicht im aktuellen Stream anwesend
            userChannelInformationStorage.save(info.withNewLastSeenAndCount())
        } else {
            //Stream ist offline
            userChannelInformationStorage.save(info.withNewLastSeen())
        }
    }

        //TODO Nutzerrolle auswerten
        //TODO Nutzerliste in Abständen auswerten
        //TODO Antwort internationalisieren
        //TODO Oberfläche? (ggf. auch erst mit dem Automod)
}

