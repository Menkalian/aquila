package de.menkalian.aquila.server

import de.menkalian.aquila.api.FrameType
import de.menkalian.aquila.api.TransferableValue
import de.menkalian.aquila.util.toHexString
import de.menkalian.aquila.util.toPrettyString
import de.menkalian.vela.generated.AquilaKey.Aquila
import io.ktor.http.cio.websocket.Frame
import io.ktor.routing.Route
import io.ktor.websocket.WebSocketServerSession
import io.ktor.websocket.webSocket
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Route.websocketRoutes() {
    webSocket("/websocket") {
        for (frame in incoming) {
            WebsocketManager.processFrame(frame, this)
        }
    }
}

abstract class WebsocketManager {
    companion object {
        val log: Logger = LoggerFactory.getLogger(WebsocketManager::class.java)

        @OptIn(ExperimentalSerializationApi::class)
        val serializer = Cbor {
            ignoreUnknownKeys = true
        }
        val subscriptions = HashMap<String, MutableList<MessageProcessor>>()

        suspend fun processFrame(frame: Frame, session: WebSocketServerSession) {
            val data = frame.data
            log.trace("Received data frame from session $session: ${frame.data.toHexString()}")
            val sentFrame = serializer.decodeFromByteArray<de.menkalian.aquila.api.Frame>(data)

            when (sentFrame.type) {
                FrameType.HEARTBEAT -> {
                    log.debug("Received Heartbeat from ${session.toPrettyString()}")
                    val returnFrame = de.menkalian.aquila.api.Frame.newHeartbeat()
                    returnFrame.setTimestamp(sentFrame.messageVariables[Aquila.Websocket.Message.Timestamp.toString()]?.asLong() ?: 0L)
                    session.outgoing.send(Frame.Binary(true, serializer.encodeToByteArray(returnFrame)))
                }
                FrameType.SUBSCRIBE -> {
                    val topic = sentFrame.messageVariables[Aquila.Websocket.Message.Topic.toString()]!!.asString()
                    log.debug("Received Subscription for \"$topic\" from ${session.toPrettyString()}")
                    addProcessor(topic, WebsocketMessageProcessor(session))
                }
                FrameType.MESSAGE   -> {
                    val topic = sentFrame.messageVariables[Aquila.Websocket.Message.Topic.toString()]!!.asString()
                    log.debug("Received Message for \"$topic\" from ${session.toPrettyString()}")
                    log.trace("Message: ${sentFrame.messageVariables}")
                    subscriptions[topic]?.forEach { it.processMessage(sentFrame.messageVariables) }
                }
            }
        }

        fun addProcessor(topic: String, processor: MessageProcessor) {
            if (subscriptions.containsKey(topic)) {
                subscriptions[topic]!!.add(processor)
            } else {
                subscriptions[topic] = mutableListOf(processor)
            }
        }
    }
}

interface MessageProcessor {
    suspend fun processMessage(message: HashMap<String, TransferableValue>)
}

class WebsocketMessageProcessor(val session: WebSocketServerSession) : MessageProcessor {
    @OptIn(ExperimentalSerializationApi::class)
    override suspend fun processMessage(message: HashMap<String, TransferableValue>) {
        val frame = de.menkalian.aquila.api.Frame.newMessage("")
        frame.messageVariables.putAll(message)
        session.outgoing.send(Frame.Binary(true, Cbor.encodeToByteArray(frame)))
    }
}