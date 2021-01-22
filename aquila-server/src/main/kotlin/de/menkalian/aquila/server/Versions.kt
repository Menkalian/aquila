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
import org.slf4j.LoggerFactory
import java.io.File

const val ARTIFACTORY_URL = "http://server.menkalian.de:8081/artifactory"
const val REPOSITORY = "aquila"
const val ARTIFACT_LOCATION = "de/menkalian/aquila"

private val log = LoggerFactory.getLogger("Versions")

@Serializable
data class Versions(val version: String)

@Suppress("ArrayInDataClass")
@Serializable
data class ArtifactoryInfo(val children: Array<ArtifactoryChildrenInfo>) {
    @Serializable
    data class ArtifactoryChildrenInfo(val uri: String, val folder: Boolean)
}

fun Route.versionRoutes() {
    get("/version/{target}") {
        log.info("Received GET-Request for '${call.request.local.uri}'")
        val targetPlatform = call.parameters["target"] ?: "api"
        if (targetPlatform == "api") {
            call.respond(Versions(API_VERSION))
        } else {
            call.respond(Versions(getLatestVersion(targetPlatform)))
        }
    }
    get("/version/{target}/update") {
        try {
            log.info("Received GET-Request for '${call.request.local.uri}'")

            log.debug("Preparing APK for update")
            val apk = loadAndVerifyApk(call.parameters["target"] ?: "android")

            // Responding with File
            log.info("Responding with verified APK")
            call.response.header(
                HttpHeaders.ContentDisposition,
                ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "update.apk").toString()
            )
            call.respondFile(apk)

            // Delete Temporary Dir
            apk.parentFile.delete()
        } catch (e: InvalidChecksumException) {
            log.warn("APK is not verified! File is not being delivered.")
            call.response.status(HttpStatusCode.InternalServerError)
            call.respond(de.menkalian.aquila.util.Error("Checksum of APK not verified. Please retry or contact the server owner!", 10))
        }
    }
}

suspend fun getLatestVersion(target: String): String {
    log.debug("Retrieving latest version for target '{}'", target)
    val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    log.trace("Starting request to Artifactory '$ARTIFACTORY_URL'")
    val response: ArtifactoryInfo = client.get("$ARTIFACTORY_URL/api/storage/$REPOSITORY/$ARTIFACT_LOCATION/app-$target")
    val version = response.children.filter { it.folder }.maxByOrNull { it.uri }!!.uri.substring(1)

    log.trace("Received Info: $response")
    log.debug("Version for $target is '$version'")
    return version
}

@Suppress("DEPRECATION")
suspend fun loadAndVerifyApk(target: String): File {
    log.trace("Loading APK for '$target' [determining latest version]")
    val version = getLatestVersion(target)
    log.debug("Loading APK for '$target' (version: '$version')")

    val client = HttpClient(CIO)
    val fileUrl = "$ARTIFACTORY_URL/$REPOSITORY/$ARTIFACT_LOCATION/app-$target/$version/app-$target-$version.zip"

    // Download zip
    log.trace("Downloading ZIP from artifactory '$ARTIFACTORY_URL'")
    val zipTemp = createTempFile("aquila", ".zip")
    client.get<HttpResponse>(fileUrl)
        .content.copyAndClose(zipTemp.writeChannel())
    log.trace("Finished downloading ZIP. Filesize: ${zipTemp.length()} B")

    // Get Checksum
    log.trace("Loading SHA512 checksum from artifactory '$ARTIFACTORY_URL'")
    val checksum = client
        .get<HttpResponse>("$fileUrl.sha512")
        .content.readUTF8Line()

    log.trace("Received Checksum '$checksum'. Comparing with file")
    if (checksum.equals(zipTemp.sha512(), true)) {
        log.trace("Checksums match! Unzipping file and delivering apk.")
        val targetDir = createTempDir("aquila-apk")
        zipTemp.unzip(targetDir)
        return targetDir.listFiles()!!.first { it.name.endsWith(".apk") }
    } else {
        log.error("ZIP checksum does not match received checksum: '$checksum'")
        throw InvalidChecksumException()
    }
}