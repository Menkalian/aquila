package de.menkalian.aquila.common.data.plugin

import kotlinx.serialization.Serializable

@Serializable
data class PluginUploadData(
    val id: String,
    val name: String,
    val description: String,
    val versiontext: String,
    val version: VersionData,
    val archived: Boolean?,
    val creator: Long,
    val createdAt: String,
    val tags: List<String>,
    val artifactData: ByteArray
)
