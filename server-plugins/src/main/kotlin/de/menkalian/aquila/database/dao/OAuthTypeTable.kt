package de.menkalian.aquila.database.dao

import de.menkalian.aquila.database.dao.shared.EnumDataTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

object OAuthTypeTable : EnumDataTable("oauth_type") {
    class Entry(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<Entry>(OAuthTypeTable)

        var name by OAuthTypeTable.name
    }
}