package de.menkalian.aquila.server.data

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document("user")
data class User(
    @Indexed(unique = false)
    var name: String,
) {
    @Id
    lateinit var id: String
    val registration: LocalDateTime = LocalDateTime.now()

    @DBRef(lazy = true)
    val friends: MutableList<User> = mutableListOf()
}
