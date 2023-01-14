package de.menkalian.aquila.database.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object PluginDataTable : LongIdTable("plugin_data") {
    val name = text("name").uniqueIndex()
    val owner = reference("owner", UserDataTable)
    val searchtext = text("searchtext")
    val attributes = reference("attributes", VariableListTable)

    class Entry(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<Entry>(PluginDataTable)

        var name by PluginDataTable.name
        var owner by UserDataTable.Entry referencedOn PluginDataTable.owner
        var searchtext by PluginDataTable.searchtext
        var attributes by VariableListTable.Entry referencedOn PluginDataTable.attributes
        val versions by PluginVersionTable.Entry referrersOn PluginVersionTable.plugin
    }
}