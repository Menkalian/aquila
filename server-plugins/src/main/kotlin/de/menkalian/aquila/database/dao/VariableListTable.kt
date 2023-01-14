package de.menkalian.aquila.database.dao

import de.menkalian.aquila.common.TransferableValue
import de.menkalian.aquila.common.ValueMap
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object VariableListTable : LongIdTable("variable_list") {
    class Entry(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<Entry>(VariableListTable)

        val entries by VariableTable.Entry referrersOn VariableTable.list

        fun toValueMap(): ValueMap {
            return entries.map {
                it.key to TransferableValue(it.type.toEnum(), it.value)
            }.toMap(HashMap())
        }
    }
}