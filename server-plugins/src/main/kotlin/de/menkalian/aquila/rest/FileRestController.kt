package de.menkalian.aquila.rest

import de.menkalian.aquila.database.PluginDatabase
import org.springframework.core.io.InputStreamResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.io.ByteArrayInputStream

@RestController
class FileRestController(
    val database: PluginDatabase
) {
    @GetMapping("/file/{uuid}")
    @ResponseBody
    fun getFile(@PathVariable uuid: String): Mono<ResponseEntity<Resource?>> {
        return Mono.create {
            val file = database.getFile(uuid)
            it.success(
                ResponseEntity.ok().header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"${file.name}.${file.extension}\""
                ).body<Resource?>(InputStreamResource(ByteArrayInputStream(file.data)))
            )
        }
    }
}
