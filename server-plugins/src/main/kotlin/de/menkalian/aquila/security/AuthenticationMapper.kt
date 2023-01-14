package de.menkalian.aquila.security

import org.springframework.security.config.oauth2.client.CommonOAuth2Provider
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.stereotype.Component

@Component
class AuthenticationMapper() {
    fun createFromOAuthToken(authentication: OAuth2AuthenticationToken): SimplifiedUserAuthentication {
        return when (authentication.authorizedClientRegistrationId) {
            CommonOAuth2Provider.GITHUB.name.lowercase() -> {
                val userAttributes = authentication.principal.attributes
                SimplifiedUserAuthentication(
                    CommonOAuth2Provider.GITHUB.name,
                    userAttributes["id"]?.toString() ?: authentication.name,
                    userAttributes["name"]?.toString(),
                    userAttributes["avatar_url"]?.toString()
                )
            }

            CommonOAuth2Provider.GOOGLE.name.lowercase() -> {
                val userAttributes = authentication.principal.attributes
                SimplifiedUserAuthentication(
                    CommonOAuth2Provider.GOOGLE.name,
                    userAttributes["sub"]?.toString() ?: authentication.name,
                    userAttributes["name"]?.toString(),
                    userAttributes["picture"]?.toString()
                )
            }

            else                             -> {
                SimplifiedUserAuthentication(
                    authentication.authorizedClientRegistrationId,
                    authentication.name,
                    null,
                    null
                )
            }
        }
    }
}