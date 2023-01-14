package de.menkalian.aquila.database.dao

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.LongIdTable

object OAuthLinkTable : LongIdTable("oauth_link") {
    val type = reference("type", OAuthTypeTable)
    val userid = text("userid").uniqueIndex()

    class Entry(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<Entry>(OAuthLinkTable)

        var type by OAuthTypeTable.Entry referencedOn OAuthLinkTable.type
        var userid by OAuthLinkTable.userid
    }
}