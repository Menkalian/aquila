package de.menkalian.aquila.database.dao

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object MetaDataTable : IntIdTable() {
    val key = varchar("key", 255).uniqueIndex()
    val value = text("value")

    class Entry(id: EntityID<Int>) : IntEntity(id) {
        companion object : IntEntityClass<Entry>(MetaDataTable)
        var key by MetaDataTable.key
        var value by MetaDataTable.value
    }
}