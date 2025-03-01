package de.arondc.pipbot.twitch.user

import org.springframework.stereotype.Service

@Service
class TwitchUserService(private val twitchUserRepository: TwitchUserRepository) {
    fun saveUser(user: TwitchUser) {
        twitchUserRepository.save(user)
    }

    fun getUser(username: String): TwitchUser? {
        return twitchUserRepository.findByUsernameIgnoreCase(username)
    }
}
