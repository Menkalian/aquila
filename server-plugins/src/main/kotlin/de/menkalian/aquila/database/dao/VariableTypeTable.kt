package de.menkalian.aquila.database.dao

import de.menkalian.aquila.common.TransferableValue
import de.menkalian.aquila.database.dao.shared.EnumDataTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

object VariableTypeTable : EnumDataTable("variable_type") {
    class Entry(id: EntityID<Long>) : LongEntity(id) {
        companion object : LongEntityClass<Entry>(VariableTypeTable) {
            fun TransferableValue.TransferableValueType.findDao(): Entry {
                return find {
                    VariableTypeTable.name.eq(this@findDao.name)
                }.first()
            }
        }

        var name by VariableTypeTable.name

        fun toEnum(): TransferableValue.TransferableValueType {
            return TransferableValue.TransferableValueType.valueOf(name)
        }
    }
}