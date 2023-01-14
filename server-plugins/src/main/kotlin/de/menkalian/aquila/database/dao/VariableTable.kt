package de.menkalian.aquila.database.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object VariableTable : LongIdTable("variable") {
    val list = reference("list", VariableListTable)
    val key = text("key")
    val value = text("value")
    val type = reference("type", VariableTypeTable)

    class Entry(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<Entry>(VariableTable)

        var list by VariableListTable.Entry referencedOn VariableTable.list
        var key by VariableTable.key
        var value by VariableTable.value
        var type by VariableTypeTable.Entry referencedOn VariableTable.type
    }
}