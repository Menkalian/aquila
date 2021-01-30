@file:Suppress("unused")

package de.menkalian.aquila

import de.menkalian.aquila.api.TransferableValue
import de.menkalian.vela.generated.AquilaKey.Aquila
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
enum class FrameType {
    HEARTBEAT, SUBSCRIBE, MESSAGE
}

@Serializable
abstract class Frame(val type: FrameType) {
    val messageVariables: HashMap<String, TransferableValue> = HashMap()
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
}