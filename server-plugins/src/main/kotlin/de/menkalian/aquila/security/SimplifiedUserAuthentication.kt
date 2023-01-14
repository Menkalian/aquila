package de.menkalian.aquila.security

data class SimplifiedUserAuthentication(
    val providerName: String,
    val userId: String,
    val suggestedName: String?,
    val suggestedAvatarUrl: String?,
)
