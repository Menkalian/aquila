package de.menkalian.aquila

import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.EnumerablePropertySource
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component


@Component
class ConfigurationManager(
    val environment: Environment,
    @Value("\${aquila.server.address.public.schema:http}")
    val publicAddressSchema: String,
    @Value("\${aquila.server.address.public.host:localhost}")
    val publicAddressHost: String,
    @Value("\${aquila.server.address.public.port:\${server.port:8080}}")
    val publicAddressPort: Long,
    @Value("\${aquila.server.address.public.path}")
    val publicAddressPathPrefix: String
) {
    companion object {
        const val DEFAULT_SETTINGS_CATEGORY = "general"
        val FORBIDDEN_KEYWORDS = listOf<String>(
            "key",
            "password",
            "secret"
        )
    }

    fun getAllPublicProperties(): Map<String, String> {
        return environment.getAllKnownProperties()
            .filter {
                FORBIDDEN_KEYWORDS.none { forbidden -> it.key.lowercase().contains(forbidden) }
            }
    }

    final fun Environment.getAllKnownProperties(): Map<String, String> {
        val rtn = HashMap<String, String>()
        if (this is ConfigurableEnvironment) {
            for (propertySource in this.propertySources) {
                if (propertySource is EnumerablePropertySource<*>) {
                    for (key in propertySource.propertyNames) {
                        rtn[key] = propertySource.getProperty(key)?.toString() ?: ""
                    }
                }
            }
        }
        return rtn
    }

    fun getFileDownloadPath(uuid: String): String {
        return formatServerPath("file/$uuid")
    }

    fun formatServerPath(path: String): String {
        return "$publicAddressSchema://$publicAddressHost:$publicAddressPort${if (publicAddressPathPrefix.isNotBlank()) "/$publicAddressPathPrefix" else ""}/$path"
    }
}