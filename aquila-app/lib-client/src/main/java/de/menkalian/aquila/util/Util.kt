package de.menkalian.aquila.util

import de.menkalian.aquila.client.VersionCompatibility

fun compareVersions(local: String, server: String): VersionCompatibility {
    return when {
        local >= server -> VersionCompatibility.FULLY_COMPATIBLE
        trimVersion(local, 1) >= trimVersion(server, 1) -> VersionCompatibility.MOSTLY_COMPATIBLE
        trimVersion(local, 2) >= trimVersion(server, 2) -> VersionCompatibility.PARTIALLY_COMPATIBLE
        else -> VersionCompatibility.INCOMPATIBLE
    }
}

fun trimVersion(version: String, amount: Int): String {
    if (amount <= 0) return version
    else return trimVersion(version.substring(0, version.lastIndexOf('.')), amount - 1)
}