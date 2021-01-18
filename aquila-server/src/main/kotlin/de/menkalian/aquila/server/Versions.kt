package de.menkalian.aquila.server

import de.menkalian.aquila.util.InvalidChecksumException
import de.menkalian.aquila.util.sha512
import de.menkalian.aquila.util.unzip
import io.ktor.application.call
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentDisposition
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.response.header
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import io.ktor.utils.io.readUTF8Line
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

const val ARTIFACTORY_URL = "http://server.menkalian.de:8081/artifactory"
const val REPOSITORY = "aquila"
const val ARTIFACT_LOCATION = "de/menkalian/aquila"

@Serializable
data class Versions(val version: String)

@Serializable
data class ArtifactoryInfo(val children: Array<ArtifactoryChildrenInfo>) {
    @Serializable
    data class ArtifactoryChildrenInfo(val uri: String, val folder: Boolean)
}

fun Route.versionRoutes() {
    get("/version/{target}") {
        val targetPlatform = call.parameters["target"] ?: "api"
        if (targetPlatform == "api") {
            call.respond(Versions("V2.0"))
        } else {
            call.respond(Versions(getLatestVersion(targetPlatform)))
        }
    }
    get("/version/{target}/update") {
        try {
            val apk = loadAndVerifyApk(call.parameters["target"] ?: "android")

            // Responding with File
            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "update.apk").toString()
            )
            call.respondFile(apk)

            // Delete Temporary Dir
            apk.parentFile.delete()
        } catch (e: InvalidChecksumException) {
            call.response.status(HttpStatusCode.InternalServerError)
            call.respond(de.menkalian.aquila.util.Error("Checksum of APK not verified. Please retry or contact the server owner!", 10))
        }
    }
}

suspend fun getLatestVersion(target: String): String {
    val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json {
                ignoreUnknownKeys = true
            })
        }
    }
    val response: ArtifactoryInfo = client.get("$ARTIFACTORY_URL/api/storage/$REPOSITORY/$ARTIFACT_LOCATION/app-$target")
    return response.children.filter { it.folder }.maxByOrNull { it.uri }!!.uri.substring(1)
}

suspend fun loadAndVerifyApk(target: String): File {
    val version = getLatestVersion(target)

    val client = HttpClient(CIO)
    val fileUrl = "$ARTIFACTORY_URL/$REPOSITORY/$ARTIFACT_LOCATION/app-$target/$version/app-$target-$version.zip"

    // Download zip
    val zipTemp = createTempFile("aquila", ".zip")
    client.get<HttpResponse>(fileUrl)
        .content.copyAndClose(zipTemp.writeChannel())

    // Get Checksum
    val checksum = client
        .get<HttpResponse>("$fileUrl.sha512")
        .content.readUTF8Line()

    if (checksum.equals(zipTemp.sha512(), true)) {
        val targetDir = createTempDir("aquila-apk")
        zipTemp.unzip(targetDir)
        return targetDir.listFiles()!!.first { it.name.endsWith(".apk") }
    } else {
        throw InvalidChecksumException()
    }
}