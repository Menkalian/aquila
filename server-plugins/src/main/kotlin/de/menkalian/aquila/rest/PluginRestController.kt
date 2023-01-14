package de.menkalian.aquila.rest

import de.menkalian.aquila.ConfigurationManager
import de.menkalian.aquila.common.data.plugin.PaginatedPluginData
import de.menkalian.aquila.common.data.plugin.PluginData
import de.menkalian.aquila.common.data.plugin.PluginFilterData
import de.menkalian.aquila.common.data.plugin.PluginUploadData
import de.menkalian.aquila.common.data.plugin.VersionData
import de.menkalian.aquila.database.PluginDatabase
import de.menkalian.aquila.security.AuthenticationMapper
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RestController
class PluginRestController(
    private val database: PluginDatabase,
    private val authenticationMapper: AuthenticationMapper,
    private val fileController: FileRestController,
    private val configurationManager: ConfigurationManager
) {
    @GetMapping("/plugins/all")
    fun findAllPlugins(@RequestParam(defaultValue = "0") start: Long, @RequestParam(defaultValue = "50") limit: Long): Mono<PaginatedPluginData> {
        return Mono.create {
            val allPlugins = database.listAllPlugins(start, limit)
            it.success(
                PaginatedPluginData(
                    allPlugins,
                    start,
                    allPlugins.size.toLong(),
                    configurationManager.formatServerPath("plugins/all?start=${start + allPlugins.size}&limit=${limit}")
                )
            )
        }
    }

    @PostMapping("/plugins/all")
    fun filterAllPlugins(
        @RequestParam(defaultValue = "0") start: Long,
        @RequestParam(defaultValue = "50") limit: Long,
        @RequestBody filter: PluginFilterData
    ): Mono<PaginatedPluginData> {
        return Mono.create {
            val allPlugins = database.listFilteredPlugins(filter, start, limit)
            it.success(
                PaginatedPluginData(
                    allPlugins,
                    start,
                    allPlugins.size.toLong(),
                    configurationManager.formatServerPath("plugins/all?start=${start + allPlugins.size}&limit=${limit}")
                )
            )
        }
    }

    @GetMapping("/plugins/{id}/{version}")
    fun findPlugin(@PathVariable id: String, @PathVariable version: String): Mono<PluginData> {
        return Mono.create {
            it.success(database.getPlugin(id, version))
        }.switchIfEmpty(Mono.error(ElementNotFoundException()))
    }

    @PostMapping("/plugins/{id}/{version}")
    fun updatePlugin(
        authenticationToken: OAuth2AuthenticationToken,
        @PathVariable id: String,
        @PathVariable version: String,
        @RequestBody updatedData: PluginData
    ): Mono<PluginData> {
        val user = authenticationMapper.createFromOAuthToken(authenticationToken)
        return Mono.create {
            val plugin = database.getPlugin(id, version) ?: kotlin.run {
                it.error(ElementNotFoundException())
                return@create
            }
            val userData = database.getUserByOAuthData(user) ?: kotlin.run {
                it.error(InsufficientUserRightsException())
                return@create
            }

            if (userData.userId == plugin.creator) {
                it.success(database.modifyPlugin(updatedData))
            } else {
                it.error(InsufficientUserRightsException())
            }
        }.switchIfEmpty(Mono.error(ElementNotFoundException()))
    }

    @PutMapping("/plugins/{id}/{version}")
    fun createPlugin(
        authenticationToken: OAuth2AuthenticationToken,
        @PathVariable id: String,
        @PathVariable version: String,
        @RequestBody createdData: PluginUploadData
    ): Mono<PluginData> {
        val user = authenticationMapper.createFromOAuthToken(authenticationToken)
        return Mono.create {
            val userData = database.getUserByOAuthData(user) ?: run {
                it.error(InsufficientUserRightsException())
                return@create
            }
            val creationData = createdData.copy(
                id = id,
                versiontext = version,
                version = VersionData.ofString(version),
                archived = false,
                createdAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                creator = userData.userId
            )
            it.success(
                database.createPlugin(creationData)
            )
        }
    }

    @DeleteMapping("/plugins/{id}/{version}")
    fun archivePlugin(
        authenticationToken: OAuth2AuthenticationToken,
        @PathVariable id: String,
        @PathVariable version: String
    ): Mono<Boolean> {
        val user = authenticationMapper.createFromOAuthToken(authenticationToken)
        return Mono.create {
            val plugin = database.getPlugin(id, version) ?: kotlin.run {
                it.error(ElementNotFoundException())
                return@create
            }
            val userData = database.getUserByOAuthData(user) ?: kotlin.run {
                it.error(InsufficientUserRightsException())
                return@create
            }

            if (userData.userId == plugin.creator) {
                it.success(database.archivePlugin(id, version) != null)
            } else {
                it.error(InsufficientUserRightsException())
            }
        }.switchIfEmpty(Mono.error(ElementNotFoundException()))
    }
}