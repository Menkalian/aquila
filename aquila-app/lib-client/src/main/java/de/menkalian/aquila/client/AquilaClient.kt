package de.menkalian.aquila.client

import android.content.Context
import de.menkalian.aquila.util.compareVersions
import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import java.io.File

class AquilaClient(val deviceType: String, val version: String, val context: Context) {
    val client: HttpClient
    val serverUrl: Url

    init {
        client = HttpClient(Android) {
            install(JsonFeature)
        }
        serverUrl = Url(context.getString(R.string.aquila_server_url))
    }

    suspend fun getApiCompatibility(): VersionCompatibility {
        return withContext(Dispatchers.IO) {
            compareVersions(SUPPORTED_API_VERSION, getVersion("api"))
        }
    }

    suspend fun updateState(): VersionCompatibility {
        return withContext(Dispatchers.IO) {
            compareVersions(version, getVersion())
        }
    }


    suspend fun prepareUpdate(): File {
        return withContext(Dispatchers.IO) {
            val response = client.get<HttpResponse>("$serverUrl/version/$deviceType/update")
            val destFolder = File(context.filesDir, "update")
            destFolder.mkdirs()
            val apk = File(destFolder, "update.apk")
            response.content.copyAndClose(apk.writeChannel())
            return@withContext apk
        }
    }

    private suspend fun getVersion(type: String = deviceType): String {
        val versioningObject: JsonObject = client.get("$serverUrl/version/$type")
        return versioningObject["version"].toString()
    }
}