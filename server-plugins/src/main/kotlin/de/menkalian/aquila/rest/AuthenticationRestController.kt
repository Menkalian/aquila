package de.menkalian.aquila.rest

import de.menkalian.aquila.common.data.plugin.UserData
import de.menkalian.aquila.common.data.plugin.UserUploadData
import de.menkalian.aquila.database.PluginDatabase
import de.menkalian.aquila.security.AuthenticationMapper
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class AuthenticationRestController(
    val authenticationMapper: AuthenticationMapper,
    val database: PluginDatabase
) {
    @GetMapping("/user")
    fun getUser(@RequestParam("id") userId: Long?): Mono<UserData> {
        return userId?.let { id ->
            Mono.create {
                it.success(database.getUserById(id) ?: throw ElementNotFoundException())
            }
        } ?: throw ElementNotFoundException()
    }

    @GetMapping("/user/me")
    fun getCurrentUser(authentication: OAuth2AuthenticationToken): Mono<UserData> {
        return Mono.create {
            val user = authenticationMapper.createFromOAuthToken(authentication)
            it.success(database.getUserByOAuthData(user) ?: throw ElementNotFoundException())
        }
    }

    @PostMapping("/user")
    fun updateCurrentUser(authentication: OAuth2AuthenticationToken, @RequestBody modifiedUserData: UserUploadData): Mono<UserData> {
        return Mono.create {
            val user = authenticationMapper.createFromOAuthToken(authentication)
            it.success(database.modifyUserWithOAuthData(user, modifiedUserData))
        }
    }

    @PutMapping("/user")
    fun createUser(authentication: OAuth2AuthenticationToken, @RequestBody userData: UserUploadData): Mono<UserData> {
        return Mono.create {
            val user = authenticationMapper.createFromOAuthToken(authentication)
            database.getUserByOAuthData(user)?.run { throw RedundantDataException() } // Check no user associated
            it.success(database.createUserFromOAuthData(user, userData))
        }
    }

    @DeleteMapping("/user")
    fun deleteUser(authentication: OAuth2AuthenticationToken): Mono<Boolean> {
        return Mono.create {
            val user = authenticationMapper.createFromOAuthToken(authentication)
            it.success(database.deleteUserByOAuthData(user) != null)
        }
    }
}