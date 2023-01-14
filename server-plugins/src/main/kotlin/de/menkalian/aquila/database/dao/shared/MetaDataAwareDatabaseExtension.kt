package de.menkalian.aquila.database.dao.shared

import de.menkalian.aquila.database.dao.MetaDataTable
import de.menkalian.aquila.generated.Aquila
import de.menkalian.aquila.logger
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.boot.info.BuildProperties
import java.time.Instant
import java.time.format.DateTimeFormatter

class MetaDataAwareDatabaseExtension {
    private val metadataChangedListeners = mutableMapOf<String, de.menkalian.aquila.database.Database.IMetaDataChangedListener>()
    private var defaultListener = de.menkalian.aquila.database.Database.IMetaDataChangedListener { _, _, _ -> true }

    fun setDefaultListener(listener: de.menkalian.aquila.database.Database.IMetaDataChangedListener) {
        synchronized(metadataChangedListeners) {
            defaultListener = listener
        }
    }

    fun setListener(key: String, listener: de.menkalian.aquila.database.Database.IMetaDataChangedListener) {
        synchronized(metadataChangedListeners) {
            metadataChangedListeners[key] = listener
        }
    }

    fun initMetadata(database: de.menkalian.aquila.database.Database, build: BuildProperties, schemaVersion: Int, name: String) {
        setListener(Aquila.Plugin.Database.CreatedAt) { _, _, _ -> false }

        val entriesToSet: MutableMap<String, String> = mutableMapOf()
        entriesToSet[Aquila.Plugin.Database.Type] = name
        entriesToSet[Aquila.Plugin.Database.Version] = schemaVersion.toString()
        entriesToSet[Aquila.Plugin.Database.Timestamp] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        entriesToSet[Aquila.Plugin.Database.CreatedAt] = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        entriesToSet[Aquila.Plugin.Build.Version] = "${build.group}:${build.artifact}:${build.version}"
        entriesToSet[Aquila.Plugin.Build.Timestamp] = DateTimeFormatter.ISO_INSTANT.format(build.time)

        database.ensureOpen()
        upsertMetadata(database.dbConnection, entriesToSet)
    }

    private fun upsertMetadata(connection: org.jetbrains.exposed.sql.Database, newMetadata: Map<String, String>) {
        transaction(connection) {
            SchemaUtils.create(MetaDataTable)

            newMetadata.forEach { (key, value) ->
                insertMetaData(key, value)
            }
        }
    }

    fun queryMetadata(connection: org.jetbrains.exposed.sql.Database, key: String): String? {
        return transaction(connection) {
            SchemaUtils.create(MetaDataTable)

            MetaDataTable
                .select(MetaDataTable.key.eq(key))
                .firstOrNull()
                ?.get(MetaDataTable.value)
        }
    }

    // MUST be called within an transaction
    private fun insertMetaData(key: String, value: String) {
        val oldEntry = MetaDataTable.Entry
            .find { MetaDataTable.key.eq(key) }
            .firstOrNull()

        if (oldEntry == null) {
            logger().debug("Adding MetaData: (\"$key\" => \"$value\")")
            MetaDataTable.Entry.new {
                this.key = key
                this.value = value
            }
            return
        }

        if (oldEntry.value != value) {
            if (fireChanged(key, oldEntry.value, value)) {
                logger().debug("Changing MetaData: (\"$key\" => \"$value\")")
                oldEntry.value = value
            } else {
                logger().debug("MetaDataChangeListener denied changing \"$key\"")
                logger().debug("Did not update MetaData: (\"$key\" => \"$value\")")
            }
        } else {
            logger().debug("Unchanged MetaData: (\"$key\" => \"$value\")")
        }
    }

    private fun fireChanged(key: String, old: String, new: String): Boolean {
        synchronized(metadataChangedListeners) {
            return metadataChangedListeners[key]?.onEntryChanged(key, old, new)
                ?: defaultListener.onEntryChanged(key, old, new)
        }
    }
}