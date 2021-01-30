package de.menkalian.aquila.api.v2

import de.menkalian.aquila.api.TransferableValue
import kotlinx.serialization.Serializable

@Serializable
data class Lobby(val id: Int, val name: String, val game: Game, val properties: HashMap<String, TransferableValue>)

@Serializable
data class LobbyLogin(val limitedTimeId: Int, val password: String)

@Serializable
data class LobbyLoginResponse(val success: Boolean, val error: Int, val msg: String, val lobbyToken: String)