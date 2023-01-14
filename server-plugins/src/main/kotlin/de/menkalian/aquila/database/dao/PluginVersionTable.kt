package de.menkalian.aquila.database.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object PluginVersionTable : LongIdTable("plugin_version") {
    val name = text("name")
    val plugin = reference("plugin", PluginDataTable)
    val creationTime = timestamp("creation_time")
    val attributes = reference("attributes", VariableListTable)
    val artifact = reference("artifact", FileDataTable)

    class Entry(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<Entry>(PluginVersionTable)

        var name by PluginVersionTable.name
        var plugin by PluginDataTable.Entry referencedOn PluginVersionTable.plugin
        var creationTime by PluginVersionTable.creationTime
        var attributes by VariableListTable.Entry referencedOn PluginVersionTable.attributes
        var artifact by FileDataTable.Entry referencedOn PluginVersionTable.artifact
    }
}