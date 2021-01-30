package de.menkalian.aquila.api

import kotlinx.serialization.Serializable

@Serializable
open class Response(val success: Boolean, val error: Int, val msg: String)