package de.arondc.pipbot.core

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.springframework.data.jpa.repository.JpaRepository

@Entity
@Table(name = "configuration")
class Configuration(
    @Id
    val id: Long = 0L,
    var username: String,
    var oAuthToken: String,
    var clientSecret: String,
    var clientId: String,
)

interface ConfigurationRepository : JpaRepository<Configuration, Long>

