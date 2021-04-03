@file:Suppress("unused")

package de.menkalian.aquila.api.v2

import de.menkalian.aquila.api.TransferableValue
import de.menkalian.vela.generated.AquilaKey.Aquila
import kotlinx.serialization.Serializable

/**
 * Class representing a player. Most values are held within the settings map.
 * Some important values for this are:
 * - Aquila.Player.Personal.Name -> Name of the player
 * - Aquila.Player.System.UUID -> UUID of the player
 * - Aquila.Player.System.SHA512 -> Hashed combination from UUID and secret Token. Used to authenticate the client
 * - Aquila.Player.System.OTID -> One-Time ID; set at login. Expires after 12h or on logout
 */
@Serializable
data class Player(val uuid: String, var state: PlayerState, val settings: HashMap<String, TransferableValue>) {
    init {
        settings[Aquila.Player.System.UUID.toString()] = TransferableValue(uuid)
    }
}

@Serializable
data class PlayerSettingChange(val otid: String, val token: ByteArray, val key: String, val value: TransferableValue)

enum class PlayerState {
    LOGOUT, IDLE, LOBBY, IN_GAME, POST_GAME
}
