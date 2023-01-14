package de.menkalian.aquila.database.dao.shared

import org.jetbrains.exposed.dao.id.LongIdTable

abstract class EnumDataTable(name: String = "", columnName: String = "id") : LongIdTable(name, columnName) {
    val name = varchar("name", 80)
}