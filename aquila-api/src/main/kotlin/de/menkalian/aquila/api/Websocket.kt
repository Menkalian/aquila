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
class Frame(val type: FrameType) {
    val messageVariables: HashMap<String, TransferableValue> = HashMap()

    @OptIn(ExperimentalSerializationApi::class)
    fun toKtorFrame(): io.ktor.http.cio.websocket.Frame =
        io.ktor.http.cio.websocket.Frame.Binary(true, Cbor.encodeToByteArray(this))

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        fun fromKtorFrame(input: io.ktor.http.cio.websocket.Frame): Frame =
            Cbor { ignoreUnknownKeys = true }
                .decodeFromByteArray(input.data)

        fun newHeartbeat() =
            Frame(FrameType.HEARTBEAT).setTimestamp(Instant.now().toEpochMilli())

        fun newSubscription(topic: String) =
            Frame(FrameType.SUBSCRIBE).setTopic(topic)

        fun newMessage(topic: String) =
            Frame(FrameType.MESSAGE).setTopic(topic)
    }

    fun setTimestamp(ts: Long): Frame {
        messageVariables[Aquila.Websocket.Message.Timestamp.toString()] = TransferableValue(ts)
        return this
    }

    fun setTopic(topic: String): Frame {
        messageVariables[Aquila.Websocket.Message.Topic.toString()] = TransferableValue(topic)
        return this
    }

    fun addValue(key: String, value: TransferableValue): Frame {
        messageVariables[key] = value
        return this
    }
}
