package de.menkalian.aquila.client

const val SUPPORTED_API_VERSION = "2.0"

enum class VersionCompatibility {
    FULLY_COMPATIBLE, MOSTLY_COMPATIBLE, PARTIALLY_COMPATIBLE, INCOMPATIBLE
}