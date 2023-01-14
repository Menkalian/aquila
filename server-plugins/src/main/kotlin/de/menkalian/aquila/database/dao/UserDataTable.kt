package de.menkalian.aquila.database.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object UserDataTable : LongIdTable("user_data") {
    val auth = reference("auth", OAuthLinkTable)
    val name = text("name")
    val userdata = reference("userdata", VariableListTable)

    class Entry(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<Entry>(UserDataTable)

        var auth by OAuthLinkTable.Entry referencedOn UserDataTable.auth
        var name by UserDataTable.name
        var userdata by VariableListTable.Entry referencedOn UserDataTable.userdata
    }
}