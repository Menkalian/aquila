@file:Suppress("unused")

package de.menkalian.aquila.api

import de.menkalian.vela.generated.AquilaKey.Aquila
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import java.time.Instant

@Serializable
enum class FrameType {
    HEARTBEAT, SUBSCRIBE, MESSAGE
}

@Serializable
abstract class Frame(val type: FrameType) {
    val messageVariables: HashMap<String, TransferableValue> = HashMap()

    @OptIn(ExperimentalSerializationApi::class)
    open fun toKtorFrame(): io.ktor.http.cio.websocket.Frame =
        io.ktor.http.cio.websocket.Frame.Binary(true, Cbor.encodeToByteArray(this))

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun fromKtorFrame(input: io.ktor.http.cio.websocket.Frame): Frame =
            Cbor { ignoreUnknownKeys = true }
                .decodeFromByteArray(input.data)
    }
}

@Serializable
class HeartbeatFrame : Frame(FrameType.HEARTBEAT) {
    init {
        messageVariables[Aquila.Websocket.Message.Timestamp.toString()] = TransferableValue(Instant.now().toEpochMilli())
    }

    fun setTimestamp(ts: Long): HeartbeatFrame {
        messageVariables[Aquila.Websocket.Message.Timestamp.toString()] = TransferableValue(ts)
        return this
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun toKtorFrame(): io.ktor.http.cio.websocket.Frame =
        io.ktor.http.cio.websocket.Frame.Binary(true, Cbor.encodeToByteArray(this))
}

@Serializable
class SubscribeFrame : Frame(FrameType.SUBSCRIBE) {
    init {
        messageVariables[Aquila.Websocket.Message.Topic.toString()] = TransferableValue("")
    }

    fun setTopic(topic: String): SubscribeFrame {
        messageVariables[Aquila.Websocket.Message.Topic.toString()] = TransferableValue(topic)
        return this
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun toKtorFrame(): io.ktor.http.cio.websocket.Frame =
        io.ktor.http.cio.websocket.Frame.Binary(true, Cbor.encodeToByteArray(this))
}

@Serializable
class MessageFrame : Frame(FrameType.MESSAGE) {
    fun setTopic(topic: String): MessageFrame {
        messageVariables[Aquila.Websocket.Message.Topic.toString()] = TransferableValue(topic)
        return this
    }

    fun addValue(key: String, value: TransferableValue): MessageFrame {
        messageVariables[key] = value
        return this
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun toKtorFrame(): io.ktor.http.cio.websocket.Frame =
        io.ktor.http.cio.websocket.Frame.Binary(true, Cbor.encodeToByteArray(this))
}