package de.menkalian.aquila.server

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.features.HttpsRedirect
import io.ktor.http.cio.websocket.pingPeriod
import io.ktor.http.cio.websocket.timeout
import io.ktor.routing.routing
import io.ktor.serialization.json
import io.ktor.websocket.WebSockets
import java.time.Duration

const val API_VERSION = "2.0"

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    install(HttpsRedirect) {
        sslPort = 8083
        permanentRedirect = false
    }
    install(ContentNegotiation) {
        json()
    }
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(5)
        timeout = Duration.ofSeconds(60)
    }

    registerRoutes()
}

fun Application.registerRoutes() {
    routing {
        versionRoutes()
        websocketRoutes()
    }
}