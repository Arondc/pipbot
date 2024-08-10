package de.arondc.pipbot.users

import de.arondc.pipbot.channels.ChannelRepository
import de.arondc.pipbot.events.TwitchPermission
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


    fun recognizeUser(channelName: String, userName: String, permissions: Set<TwitchPermission>) {
        val user = userStorage.findByName(userName) ?: userStorage.save(UserEntity(userName))

        val channel = channelRepository.findByNameIgnoreCase(channelName)!!
        var info = userChannelInformationStorage.findByUserNameIgnoreCaseAndChannelNameIgnoreCase(userName, channelName)
            ?: userChannelInformationStorage.save(UserChannelInformationEntity(user, channel))

        val stream = streamService.findCurrentStream(channelName)

        //TODO diese Updates direkt über das Repo laufen lassen
        info = updateHighestUserLevel(permissions, info)
        info = updateLastSeenOfUser(info, stream)
        userChannelInformationStorage.save(info)
    }

    private fun updateHighestUserLevel(
        permissions: Set<TwitchPermission>,
        info: UserChannelInformationEntity
    ): UserChannelInformationEntity {
        val highestLevel = permissions.maxByOrNull{ it.level } ?: TwitchPermission.EVERYONE
        return info.withHighestUserLevel(highestLevel)
    }

    private fun updateLastSeenOfUser(
        info: UserChannelInformationEntity,
        stream: StreamEntity?
    ) : UserChannelInformationEntity {
        val lastSeen = info.lastSeen
        return if(stream != null && (lastSeen == null || lastSeen.isBefore(stream.startTimes.min()))) {
            //Stream ist online und Nutzer neu oder noch nicht im aktuellen Stream anwesend
            info.withNewLastSeenAndCount()
        } else {
            info.withNewLastSeen()
        }
    }

        //TODO Auswerten ob jemand Follower ist (und seit wann)
        //TODO Nutzerliste in Abständen auswerten
        //TODO Antwort internationalisieren
        //TODO Oberfläche? (ggf. auch erst mit dem Automod)
        //TODO Doku: ER/JPA & Messaging & ggf Komponenten Diagram
        //TODO Tests Tests Tests!
}

