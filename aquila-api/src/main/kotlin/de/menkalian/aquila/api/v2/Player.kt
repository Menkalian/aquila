@file:Suppress("unused")

package de.menkalian.aquila.api.v2

import de.menkalian.aquila.api.TransferableValue
import kotlinx.serialization.Serializable

@Serializable
data class Player(val name: String, val lobbyId: String, val state: PlayerState, val settings: HashMap<String, TransferableValue>)

enum class PlayerState {
    LOGOUT, IDLE, LOBBY, IN_GAME, POST_GAME
}
