package de.menkalian.aquila

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PluginServerApplication {
}

fun main(args: Array<String>) {
    runApplication<PluginServerApplication>(*args)
}