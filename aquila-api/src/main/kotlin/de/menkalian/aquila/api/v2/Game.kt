package de.menkalian.aquila.api.v2

import de.menkalian.aquila.api.TransferableValue
import kotlinx.serialization.Serializable

@Serializable
data class GameLight(val gameId: String)

@Serializable
data class Game(val gameId: String, val properties: HashMap<String, TransferableValue>)