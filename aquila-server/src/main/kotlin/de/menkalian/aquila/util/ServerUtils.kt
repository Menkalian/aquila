package de.menkalian.aquila.util

import io.ktor.features.origin
import io.ktor.websocket.WebSocketServerSession

internal fun ByteArray.toHexString(): String =
    map { String.format("%2x", it) }.reduce { full, part -> full + part }

internal fun WebSocketServerSession.toPrettyString(): String = "Session #${hashCode()} from ${this.call.request.origin.remoteHost}"