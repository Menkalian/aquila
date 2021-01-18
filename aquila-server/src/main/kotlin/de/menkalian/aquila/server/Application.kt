package de.menkalian.aquila.server

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.HttpsRedirect
import io.ktor.routing.routing
import io.ktor.serialization.json

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(HttpsRedirect) {
        sslPort = 8083
        permanentRedirect = false
    }
    install(ContentNegotiation) {
        json()
    }

}
