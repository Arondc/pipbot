package de.arondc.pipbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableAsync


fun main(args: Array<String>) {
    runApplication<PipbotApplication>(*args)
}

@SpringBootApplication
@EnableAsync
class PipbotApplication