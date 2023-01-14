package de.menkalian.aquila.common.data.plugin

import kotlinx.serialization.Serializable

@Serializable
data class UserData(
    val userId: Long,
    val name: String,
    val avatarUrl: String?
)
