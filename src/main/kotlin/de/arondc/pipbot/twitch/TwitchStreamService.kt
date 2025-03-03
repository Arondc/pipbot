package de.arondc.pipbot.twitch

import com.netflix.hystrix.exception.HystrixRuntimeException
import de.arondc.pipbot.features.Feature
import de.arondc.pipbot.features.FeatureService
import mu.KotlinLogging
import org.apache.commons.lang3.exception.ContextedRuntimeException
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

class TwitchException(message: String, cause: Throwable) : RuntimeException(message, cause)

@Service
class TwitchStreamService(
    private val twitchConnector: TwitchConnector,
    private val featureService: FeatureService
) {
    private val log = KotlinLogging.logger {}

    fun fetchCurrentStreamForChannel(channelName: String) : TwitchStream? =
        fetchCurrentStreamsForChannels(listOf(channelName)).firstOrNull()


    fun fetchCurrentStreamsForChannels(channelNames: List<String>): List<TwitchStream> {
        val streamList = twitchConnector.getStreams(channelNames)
        log.debug { "Found streams for $channelNames - $streamList" }

        return streamList.streams.map {
            TwitchStream(
                userName = it.userName,
                userLogin = it.userLogin,
                startingTime = LocalDateTime.ofInstant(it.startedAtInstant, ZoneId.systemDefault())
            )
        }.toList()
    }

    fun findLastGameFor(channelName: String): String {
        return twitchConnector.getChannelInformation(channelName).channels[0].gameName
    }

    fun shoutout(raidedChannel: String, incomingRaid: String) {
        if (incomingRaid.isNotEmpty()) {
            twitchConnector.sendShoutout(raidedChannel, incomingRaid)
        }
    }

    fun getFollowerSince(channelName: String, userName: String): LocalDateTime? {
        val followedAt: Instant? = try {
            twitchConnector.getChannelFollowers(channelName, userName).follows?.getOrNull(0)?.followedAt
        } catch (e: Exception) {
            when (e) {
                is TwitchConnector.MissingScopeException -> handleMissingAuthorizationForMissingScope(e.scope)
                else -> log.error { e.message }
            }
            throw TwitchException("Could not fetch follow information for user $userName in channel $channelName", e)
        }

        return when {
            followedAt == null -> null
            else -> LocalDateTime.ofInstant(followedAt,ZoneOffset.systemDefault())
        }
    }

    fun handleMissingAuthorizationForMissingScope(missingScope: TwitchScope) {
        log.warn { "Missing scope `${missingScope.scopeName}`"}
        when(missingScope) {
            TwitchScope.MODERATOR_READ_FOLLOWERS -> {
                featureService.disable(Feature.UpdateFollowerStatus)
                //TODO: Feature zu Scope mapping (wenn ein scope fehlt, deaktiviere alle zugehörigen features)
            }
            else -> log.warn { "We dont know what to do yet if ${missingScope.scopeName} is missing" }
        }
    }

    fun getChatUserList(channelName: String): List<TwitchChatter> {
        val chatList = twitchConnector.getChatters(channelName)
        log.debug {chatList}
        return chatList.chatters.map {
            TwitchChatter(
                userName = it.userName,
            )
        }
    }

    fun banUser(channelName: String, userName: String) {
        log.info {"Banning `$userName` in channel $channelName"}
        try {
            twitchConnector.banUser(channelName, userName)
        } catch (e: HystrixRuntimeException) {
            when(e.cause) {
                is ContextedRuntimeException -> {
                    if(e.cause!!.message!!.contains("The user specified in the user_id field is already banned."))
                    {
                        log.info {"User `$userName` already banned in channel $channelName."}
                    }
                    else throw e
                }
                else -> throw e
            }
        }
    }

    fun banUsers(channelName: String, userNames: Set<String>) {
        userNames.forEach {banUser(channelName, it) }
    }

}