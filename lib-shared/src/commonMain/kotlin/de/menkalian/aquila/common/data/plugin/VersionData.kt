package de.menkalian.aquila.common.data.plugin

import kotlinx.serialization.Serializable

@Serializable
data class VersionData(
    val major: Long,
    val minor: Long?,
    val patch: Long?
) {
    companion object {
        fun ofString(version: String): VersionData {
            val regex = "(\\d+)(\\.(\\d+)(\\.(\\d+))?)?\\S*".toRegex()
            return regex.matchEntire(version)?.groups?.let {
                VersionData(
                    it[1]?.toString()?.toLongOrNull() ?: 0,
                    it[2]?.toString()?.toLongOrNull(),
                    it[3]?.toString()?.toLongOrNull()
                )
            } ?: throw IllegalArgumentException()
        }
    }
}
