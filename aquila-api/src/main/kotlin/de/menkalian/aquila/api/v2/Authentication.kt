package de.menkalian.aquila.api.v2

import kotlinx.serialization.Serializable

@Serializable
data class PlayerRegistration(val name: String, val token: ByteArray)

@Serializable
data class PlayerRegistrationResponse(val uuid: String)

@Serializable
data class PlayerLogin(val uuid: String, val token: ByteArray)

@Serializable
class PlayerLoginResponse(val success: Boolean, val error: Int, val msg: String, val limitedId: Int)