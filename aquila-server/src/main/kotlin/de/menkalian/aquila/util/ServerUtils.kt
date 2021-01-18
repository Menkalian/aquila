package de.menkalian.aquila.util

import kotlinx.serialization.Serializable

@Serializable
data class Error(val description: String, val code: Int = 1)