package de.menkalian.aquila.database.dao

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.util.UUID

object FileDataTable : UUIDTable("file_data") {
    val name = text("name").nullable()
    val extension = text("extension").nullable()
    val createdAt = timestamp("created_at")
    val data = binary("data")

    class Entry(id: EntityID<UUID>) : UUIDEntity(id) {
        companion object : UUIDEntityClass<Entry>(FileDataTable)

        var name by FileDataTable.name
        var extension by FileDataTable.extension
        var createdAt by FileDataTable.createdAt
        var data by FileDataTable.data
    }
}