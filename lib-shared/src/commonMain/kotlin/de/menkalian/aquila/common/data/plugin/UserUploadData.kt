package de.menkalian.aquila.common.data.plugin

import kotlinx.serialization.Serializable

@Serializable
data class UserUploadData(
    val userId: Long,
    val name: String,
    val avatarData: ByteArray?
)
