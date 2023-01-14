package de.menkalian.aquila.database

import de.menkalian.aquila.ConfigurationManager
import de.menkalian.aquila.common.TransferableValue
import de.menkalian.aquila.common.ValueMap
import de.menkalian.aquila.common.data.plugin.PluginData
import de.menkalian.aquila.common.data.plugin.PluginFilterData
import de.menkalian.aquila.common.data.plugin.PluginUploadData
import de.menkalian.aquila.common.data.plugin.UserData
import de.menkalian.aquila.common.data.plugin.UserUploadData
import de.menkalian.aquila.common.data.plugin.VersionData
import de.menkalian.aquila.common.util.insertIndex
import de.menkalian.aquila.database.dao.FileDataTable
import de.menkalian.aquila.database.dao.MetaDataTable
import de.menkalian.aquila.database.dao.OAuthLinkTable
import de.menkalian.aquila.database.dao.OAuthTypeTable
import de.menkalian.aquila.database.dao.PluginDataTable
import de.menkalian.aquila.database.dao.PluginVersionTable
import de.menkalian.aquila.database.dao.ServerDataTable
import de.menkalian.aquila.database.dao.UserDataTable
import de.menkalian.aquila.database.dao.VariableListTable
import de.menkalian.aquila.database.dao.VariableTable
import de.menkalian.aquila.database.dao.VariableTypeTable
import de.menkalian.aquila.database.dao.VariableTypeTable.Entry.Companion.findDao
import de.menkalian.aquila.database.dao.shared.MetaDataAwareDatabaseExtension
import de.menkalian.aquila.generated.Plugin
import de.menkalian.aquila.generated.User
import de.menkalian.aquila.logger
import de.menkalian.aquila.rest.ElementNotFoundException
import de.menkalian.aquila.rest.InsufficientUserRightsException
import de.menkalian.aquila.rest.RedundantDataException
import de.menkalian.aquila.security.SimplifiedUserAuthentication
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.javatime.JavaInstantColumnType
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.info.BuildProperties
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID


private const val DELETED_USER_PROVIDER_NAME = "DELETED_USER_DUMMY_PROVIDER"

@Component
@Suppress("LeakingThis")
class PluginDatabaseImpl(
    @Value("\${aquila.postgres.database.host}") private val databaseHost: String,
    @Value("\${aquila.postgres.database.port}") private val databasePort: String,
    @Value("\${aquila.postgres.database.user.name}") private val databaseUsername: String,
    @Value("\${aquila.postgres.database.user.password}") private val databasePassword: String,
    @Value("\${aquila.postgres.database.plugins.name}") private val databaseName: String,
    private val configurationManager: ConfigurationManager,
    build: BuildProperties
) : PluginDatabase {
    companion object {
        private const val DATABASE_SCHEMA_VERSION = 1
    }

    override var isOpen: Boolean = false
    override val dbConnection: org.jetbrains.exposed.sql.Database

    private val metadataExtension = MetaDataAwareDatabaseExtension()

    init {
        dbConnection = org.jetbrains.exposed.sql.Database.connect(
            "jdbc:postgresql://$databaseHost:$databasePort/$databaseName",
            driver = "org.postgresql.Driver",
            user = databaseUsername,
            password = databasePassword
        )
        isOpen = true

        transaction(dbConnection) {
            createAllTables()
        }
        metadataExtension.initMetadata(this, build, DATABASE_SCHEMA_VERSION, "Plugins")
        initEnums()
        configurationManager.getAllPublicProperties()
            .forEach {
                storeSetting(ConfigurationManager.DEFAULT_SETTINGS_CATEGORY, it.key, TransferableValue.Companion.from(it.value))
            }

    }

    private fun initEnums() {
        ensureOpen()
        initEnumDatabase(dbConnection, VariableTypeTable, TransferableValue.TransferableValueType.values().map { it.name })
    }

    override fun listAllPlugins(start: Long, limit: Long): List<PluginData> {
        ensureOpen()

        return transaction(dbConnection) {
            PluginVersionTable.Entry.all()
                .limit(limit.toInt(), offset = start)
                .map(this@PluginDatabaseImpl::readPluginDataFromDatabaseEntity)
                .toList()
        }
    }

    override fun listFilteredPlugins(filterData: PluginFilterData, start: Long, limit: Long): List<PluginData> {
        ensureOpen()

        return transaction(dbConnection) {
            val sqlBuilder = StringBuilder()
            val argumentList = mutableListOf<Pair<IColumnType, Any?>>()
            sqlBuilder.append(
                """
                SELECT ${PluginVersionTable.columns.map { "V.${it.name}" }.joinToString(", ")}
                FROM ${PluginVersionTable.tableName} V
                INNER JOIN ${PluginDataTable.tableName} D ON V.${PluginVersionTable.plugin.name} = D.${PluginDataTable.id.name}
                WHERE TRUE
                
            """.trimIndent()
            )

            if (filterData.searchterm != null) {
                sqlBuilder.append("AND D.${PluginDataTable.searchtext.name} @@ ?\n")
                argumentList.add(TextColumnType() to filterData.searchterm)
            }
            if (filterData.creator != null) {
                sqlBuilder.append("AND D.${PluginDataTable.owner.name} = ?\n")
                argumentList.add(LongColumnType() to filterData.creator)
            }
            if (filterData.tags != null) {
                sqlBuilder.append(
                    """
                AND ? = (
                    SELECT COUNT(*)
                    FROM ${VariableTable.tableName} V 
                    WHERE V.${VariableTable.list.name} = D.${PluginDataTable.attributes.name}
                    AND V.${VariableTable.key.name} like '${Plugin.Information.Tags.XXX.Text.replace("XXX", "%")}'
                    AND V.${VariableTable.value.name} in (${filterData.tags?.map { "'$it'" }?.joinToString(",")})
                )
                
                """.trimIndent()
                )
                argumentList.add(IntegerColumnType() to (filterData.tags?.size ?: 0))
            }
            if (filterData.timerange != null) {
                sqlBuilder.append("AND ? < V.${PluginVersionTable.creationTime.name} AND V.${PluginVersionTable.creationTime.name} < ?\n")
                argumentList.add(
                    JavaInstantColumnType() to getInstantValue(filterData.timerange?.from, LocalDateTime.MIN)
                )
                argumentList.add(
                    JavaInstantColumnType() to getInstantValue(filterData.timerange?.until, LocalDateTime.MAX)
                )
            }

            sqlBuilder.append("OFFSET $start LIMIT $limit;")

            logger().info(
                "Filtering plugins with SQL-Statement \n\"{}\"\n and parameters \n{}.",
                sqlBuilder.toString().prependIndent(" ".repeat(4)),
                argumentList
            )

            exec(sqlBuilder.toString(), argumentList) { rs ->
                buildList<PluginVersionTable.Entry> {
                    while (rs.next()) {
                        val row = ResultRow.create(rs, PluginVersionTable.columns.map { it to rs.findColumn(it.name) }.toMap())
                        PluginVersionTable.Entry.wrapRow(row)
                    }
                }
            }?.map { readPluginDataFromDatabaseEntity(it) }
                ?: listOf()
        }
    }

    private fun getInstantValue(from: String?, min: LocalDateTime): Instant {
        return LocalDateTime.parse(
            from ?: min.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        ).let {
            it.toInstant(ZoneId.systemDefault().rules.getOffset(it))
        }
    }

    override fun getPlugin(id: String, version: String): PluginData? {
        ensureOpen()

        return transaction(dbConnection) {
            (PluginVersionTable innerJoin PluginDataTable)
                .select {
                    (PluginDataTable.name eq id).and { PluginVersionTable.name eq version }
                }.firstOrNull()?.let {
                    readPluginDataFromDatabaseEntity(PluginVersionTable.Entry.wrapRow(it))
                }
        }
    }

    override fun createPlugin(data: PluginUploadData): PluginData {
        ensureOpen()

        return transaction(dbConnection) {
            var pluginData = PluginDataTable.Entry.find { PluginDataTable.name eq data.id }.firstOrNull()

            if (pluginData == null) {
                pluginData = PluginDataTable.Entry.new {
                    this.name = data.id
                    this.owner = UserDataTable.Entry.findById(data.creator)!!
                    this.attributes = VariableListTable.Entry.new { }
                    this.searchtext = "" // TODO: Name, description, Tags storen und in searchtext packen
                }
            } else {
                if (pluginData.owner.id.value != data.creator) {
                    throw InsufficientUserRightsException()
                }

                if (getPlugin(data.id, data.versiontext) != null) {
                    throw RedundantDataException()
                }
            }

            val attributes: ValueMap = HashMap()
            attributes[Plugin.Information.Readable.Name] = TransferableValue.from(data.name)
            attributes[Plugin.Information.Readable.Description] = TransferableValue.from(data.description)
            attributes[Plugin.Information.Tags.n] = TransferableValue.from(data.tags.size)
            data.tags.forEachIndexed { idx, tag ->
                attributes[Plugin.Information.Tags.XXX.Text.insertIndex(idx + 1)] = TransferableValue.from(tag)
            }
            storeVarList(pluginData.attributes, attributes)
            pluginData.searchtext = "${data.name} ${data.description} ${data.id} ${data.versiontext} ${data.tags}"

            PluginVersionTable.Entry.new {
                this.name = data.versiontext
                this.plugin = pluginData
                this.creationTime = Instant.now()
                this.attributes = VariableListTable.Entry.new { }
                this.artifact = createFile("plugin", "jar", data.artifactData)
            }.let { this@PluginDatabaseImpl.readPluginDataFromDatabaseEntity(it) }
        }
    }

    override fun modifyPlugin(data: PluginData): PluginData {
        ensureOpen()

        return transaction(dbConnection) {
            val pluginData = PluginDataTable.Entry.find { PluginDataTable.name eq data.id }.firstOrNull()
            if (pluginData == null || pluginData.versions.none { it.name == data.versiontext }) {
                throw ElementNotFoundException()
            } else {
                if (pluginData.owner.id.value != data.creator) {
                    throw InsufficientUserRightsException()
                }

                if (getPlugin(data.id, data.versiontext) != null) {
                    throw RedundantDataException()
                }
            }

            val attributes: ValueMap = HashMap()
            attributes[Plugin.Information.Readable.Name] = TransferableValue.from(data.name)
            attributes[Plugin.Information.Readable.Description] = TransferableValue.from(data.description)
            attributes[Plugin.Information.Tags.n] = TransferableValue.from(data.tags.size)
            data.tags.forEachIndexed { idx, tag ->
                attributes[Plugin.Information.Tags.XXX.Text.insertIndex(idx + 1)] = TransferableValue.from(tag)
            }
            storeVarList(pluginData.attributes, attributes)
            pluginData.searchtext = "${data.name} ${data.description} ${data.id} ${data.versiontext} ${data.tags}"

            pluginData.versions
                .first { it.name == data.versiontext }.let { this@PluginDatabaseImpl.readPluginDataFromDatabaseEntity(it) }
        }
    }

    override fun archivePlugin(id: String, version: String): PluginData? {
        ensureOpen()

        return transaction(dbConnection) {
            (PluginVersionTable innerJoin PluginDataTable)
                .select {
                    (PluginDataTable.name eq id).and { PluginVersionTable.name eq version }
                }.firstOrNull()?.let {
                    val pluginVersion = PluginVersionTable.Entry.wrapRow(it)
                    val attributes = pluginVersion.attributes.toValueMap()
                    attributes[Plugin.Information.Archived] = TransferableValue.Companion.from(true)
                    storeVarList(pluginVersion.attributes, attributes)
                    readPluginDataFromDatabaseEntity(pluginVersion)
                }
        }
    }

    override fun getUserById(id: Long): UserData? {
        ensureOpen()

        return transaction(dbConnection) {
            try {
                readUserDataFromDatabaseEntity(UserDataTable.Entry[id])
            } catch (ex: Exception) {
                null
            }
        }
    }

    override fun getUserByOAuthData(user: SimplifiedUserAuthentication): UserData? {
        ensureOpen()

        return transaction(dbConnection) {
            (UserDataTable innerJoin OAuthLinkTable innerJoin OAuthTypeTable)
                .select { (OAuthLinkTable.userid eq user.userId).and { OAuthTypeTable.name eq user.providerName } }
                .firstOrNull()
                ?.let { UserDataTable.Entry.wrapRow(it) }
                ?.let { readUserDataFromDatabaseEntity(it) }
        }
    }

    override fun createUserFromOAuthData(user: SimplifiedUserAuthentication, userData: UserUploadData): UserData {
        ensureOpen()
        if (getUserByOAuthData(user) != null) {
            throw RedundantDataException()
        }

        return transaction(dbConnection) {
            val avatarData = if (userData.avatarData?.isEmpty() != false) {
                this::class.java.classLoader.getResourceAsStream("images/default.png")?.readAllBytes()
                    ?: throw RuntimeException("Default avatar could not be loaded")
            } else {
                userData.avatarData!!
            }

            initEnumDatabase(dbConnection, OAuthTypeTable, listOf(user.providerName))

            val file = createFile("avatar", "png", avatarData)
            val link = OAuthLinkTable.Entry.new {
                this.userid = user.userId
                this.type = OAuthTypeTable.Entry.find { OAuthTypeTable.name eq user.providerName }.first()
            }
            val attributes = VariableListTable.Entry.new { }
            val attrMap: ValueMap = HashMap()
            attrMap[User.Avatar.UUID] = TransferableValue.Companion.from(file.id.value.toString())
            storeVarList(attributes, attrMap)

            UserDataTable.Entry.new {
                this.name = userData.name
                this.auth = link
                this.userdata = attributes
            }.let {
                readUserDataFromDatabaseEntity(it)
            }
        }
    }

    override fun modifyUserWithOAuthData(user: SimplifiedUserAuthentication, userData: UserUploadData): UserData {
        ensureOpen()

        val originalUserData = getUserByOAuthData(user) ?: throw InsufficientUserRightsException()
        return transaction(dbConnection) {
            val entry = UserDataTable.Entry.findById(originalUserData.userId) ?: throw ElementNotFoundException()
            val avatar = getFile(
                entry.userdata.toValueMap()[User.Avatar.UUID]?.toString() ?: ""
            )
            entry.name = userData.name

            if (userData.avatarData == null || userData.avatarData?.isEmpty() != false) {
                val avatarData = if (userData.avatarData?.isEmpty() != false) {
                    this::class.java.classLoader.getResourceAsStream("images/default.png")?.readAllBytes()
                        ?: throw RuntimeException("Default avatar could not be loaded")
                } else {
                    userData.avatarData!!
                }
                avatar.delete()
                val newAvatar = createFile("avatar", "png", avatarData)
                entry.userdata.toValueMap().apply {
                    this[User.Avatar.UUID] = TransferableValue.Companion.from(newAvatar.id.value.toString())
                    storeVarList(entry.userdata, this)
                }
            }
            readUserDataFromDatabaseEntity(entry)
        }
    }

    override fun deleteUserByOAuthData(user: SimplifiedUserAuthentication): UserData? {
        ensureOpen()

        val existingUserData = getUserByOAuthData(user) ?: return null
        val deletedUserData = UserUploadData(
            existingUserData.userId,
            "Deleted User",
            null
        )
        modifyUserWithOAuthData(user, deletedUserData)

        return transaction(dbConnection) {
            initEnumDatabase(dbConnection, OAuthTypeTable, listOf(DELETED_USER_PROVIDER_NAME))
            val deleteProvider = OAuthTypeTable.Entry.find { OAuthTypeTable.name eq user.providerName }.first()
            val deleteEntrySearch = OAuthLinkTable.Entry.find { (OAuthLinkTable.type eq deleteProvider.id).and { OAuthLinkTable.userid eq "" } }
            val deletedLink = if (deleteEntrySearch.empty()) {
                OAuthLinkTable.Entry.new {
                    this.type = deleteProvider
                    this.userid = ""
                }
            } else {
                deleteEntrySearch.first()
            }

            val userEntry = UserDataTable.Entry[existingUserData.userId]
            val tmp = userEntry.auth
            userEntry.auth = deletedLink
            tmp.delete()
            existingUserData
        }
    }

    private fun createFile(filename: String?, extension: String?, data: ByteArray): FileDataTable.Entry {
        ensureOpen()

        return transaction(dbConnection) {
            FileDataTable.Entry.new {
                this.name = filename
                this.extension = extension
                this.createdAt = Instant.now()
                this.data = data
            }
        }
    }

    override fun getFile(uuid: String): FileDataTable.Entry {
        ensureOpen()

        return transaction(dbConnection) {
            FileDataTable.Entry.findById(UUID.fromString(uuid)) ?: throw ElementNotFoundException()
        }
    }

    private fun storeVarList(entry: VariableListTable.Entry, varList: ValueMap) {
        transaction(dbConnection) {
            val existingList = entry.toValueMap()
            val newEntries = varList.filterKeys { existingList.containsKey(it).not() }
            val changedEntries = varList.filter { ent -> existingList.containsKey(ent.key) && existingList[ent.key] != ent.value }
            val deleteEntries = existingList.filterKeys { existingList.containsKey(it).not() }

            entry.entries.filter {
                changedEntries.containsKey(it.key) || deleteEntries.containsKey(it.key)
            }.forEach {
                it.delete()
            }

            newEntries.forEach {
                VariableTable.Entry.new {
                    this.list = entry
                    this.key = it.key
                    this.value = it.value.value
                    this.type = it.value.type.findDao()
                }
            }
            changedEntries.forEach {
                VariableTable.Entry.new {
                    this.list = entry
                    this.key = it.key
                    this.value = it.value.value
                    this.type = it.value.type.findDao()
                }
            }
        }
    }

    override fun getSettings(category: String): ValueMap {
        ensureOpen()

        return transaction(dbConnection) {
            ServerDataTable.Entry.find {
                ServerDataTable.configurationGroup eq category
            }.firstOrNull()
                ?.attributes
                ?.toValueMap()
                ?: HashMap()
        }
    }

    override fun storeSetting(category: String, key: String, value: TransferableValue): Boolean {
        ensureOpen()

        return transaction(dbConnection) {
            var configGroup = ServerDataTable.Entry.find {
                ServerDataTable.configurationGroup eq category
            }.firstOrNull()
            if (configGroup == null) {
                configGroup = ServerDataTable.Entry.new {
                    configurationGroup = category
                    attributes = VariableListTable.Entry.new {}
                }
            }

            val existingEntry = configGroup.attributes.entries
                .firstOrNull {
                    it.key == key
                }

            if (existingEntry != null) {
                existingEntry.value = value.value
                existingEntry.type = value.type.findDao()
            } else {
                VariableTable.Entry.new {
                    this.list = configGroup.attributes
                    this.key = key
                    this.value = value.value
                    this.type = value.type.findDao()
                }
            }

            true
        }
    }

    private fun createAllTables() {
        SchemaUtils.create(
            FileDataTable,
            MetaDataTable,
            OAuthLinkTable,
            OAuthTypeTable,
            PluginDataTable,
            PluginVersionTable,
            ServerDataTable,
            UserDataTable,
            VariableTable,
            VariableListTable,
            VariableTypeTable
        )
    }

    override fun close() {
        isOpen = false
        TransactionManager.closeAndUnregister(dbConnection)
    }

    private fun readPluginDataFromDatabaseEntity(pluginVersion: PluginVersionTable.Entry): PluginData {
        val attrs = pluginVersion.plugin.attributes.toValueMap()
        attrs.putAll(
            pluginVersion.attributes.toValueMap()
        )

        return PluginData(
            pluginVersion.plugin.name,
            attrs[Plugin.Information.Readable.Name]?.toString() ?: pluginVersion.plugin.name,
            attrs[Plugin.Information.Readable.Description]?.toString() ?: "",
            pluginVersion.name,
            VersionData.ofString(pluginVersion.name),
            attrs[Plugin.Information.Archived]?.toBoolean() ?: false,
            pluginVersion.plugin.owner.id.value,
            LocalDateTime.ofInstant(pluginVersion.creationTime, ZoneId.systemDefault()).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            buildList {
                val count = attrs[Plugin.Information.Tags.n]?.toInt() ?: 0
                for (i in 0..count) {
                    add(attrs[Plugin.Information.Tags.XXX.Text.insertIndex(i)].toString())
                }
            },
            configurationManager.formatServerPath("file/${pluginVersion.artifact.id.value}")
        )
    }

    private fun readUserDataFromDatabaseEntity(userData: UserDataTable.Entry): UserData {
        val attrs = userData.userdata.toValueMap()

        return UserData(
            userData.id.value,
            userData.name,
            configurationManager.formatServerPath("file/${attrs[User.Avatar.UUID]}")
        )
    }
}
