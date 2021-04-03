package de.menkalian.aquila.server.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import java.time.LocalDateTime

data class Auth(@DBRef val user: User, @Indexed var expectedHash : ByteArray) {
    @Id
    lateinit var id : String
    var validUntil: LocalDateTime = LocalDateTime.now().plusDays(1)
}
