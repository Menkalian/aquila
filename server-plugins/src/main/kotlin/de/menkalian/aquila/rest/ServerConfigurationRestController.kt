package de.menkalian.aquila.rest

import de.menkalian.aquila.ConfigurationManager
import de.menkalian.aquila.common.data.plugin.ServerData
import de.menkalian.aquila.database.PluginDatabase
import org.springframework.security.oauth2.client.registration.ClientRegistration
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class ServerConfigurationRestController(
    val database: PluginDatabase,
    val clientRegRepo: ReactiveClientRegistrationRepository
) {
    @GetMapping("/server/config")
    fun getServerConfig(): Mono<ServerData> {
        return Mono.create {
            it.success(
                database.getSettings(ConfigurationManager.DEFAULT_SETTINGS_CATEGORY)
                    .mapValues {
                        it.value.value
                    }
                    .toSortedMap()
            )
        }
    }

    @GetMapping("/server/authentication")
    fun getAuthenticationProviders(): Mono<Map<String, String>> {
        return Mono.create { sink ->
            (clientRegRepo as Iterable<ClientRegistration>).map {
                it.registrationId to "/oauth2/authorization/${it.registrationId}"
            }.toMap().apply { sink.success(this) }
        }
    }
}