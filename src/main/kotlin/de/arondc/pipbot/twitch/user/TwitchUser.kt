package de.arondc.pipbot.twitch.user

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TwitchUserRepository : JpaRepository<TwitchUser, String> {
    fun findByUsernameIgnoreCase(username: String): TwitchUser?
}

@Entity
@Table(name = "twitch_user")
class TwitchUser(
    @Id
    val username: String,
    val id: String
)
