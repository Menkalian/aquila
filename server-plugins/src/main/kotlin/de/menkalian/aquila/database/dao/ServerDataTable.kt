package de.menkalian.aquila.database.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object ServerDataTable : LongIdTable("server_data") {
    val configurationGroup = text("configuration_group").uniqueIndex()
    val attributes = reference("attributes", VariableListTable)

    class Entry(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<Entry>(ServerDataTable)

        var configurationGroup by ServerDataTable.configurationGroup
        var attributes by VariableListTable.Entry referencedOn ServerDataTable.attributes
    }
}