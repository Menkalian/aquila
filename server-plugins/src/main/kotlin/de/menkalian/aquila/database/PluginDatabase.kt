package de.menkalian.aquila.database

import de.menkalian.aquila.common.TransferableValue
import de.menkalian.aquila.common.ValueMap
import de.menkalian.aquila.common.data.plugin.PluginData
import de.menkalian.aquila.common.data.plugin.PluginFilterData
import de.menkalian.aquila.common.data.plugin.PluginUploadData
import de.menkalian.aquila.common.data.plugin.UserData
import de.menkalian.aquila.common.data.plugin.UserUploadData
import de.menkalian.aquila.database.dao.FileDataTable
import de.menkalian.aquila.security.SimplifiedUserAuthentication

interface PluginDatabase : Database {
    fun listAllPlugins(start: Long, limit: Long): List<PluginData>
    fun listFilteredPlugins(filterData: PluginFilterData, start: Long, limit: Long): List<PluginData>
    fun getPlugin(id: String, version: String): PluginData?
    fun createPlugin(data: PluginUploadData): PluginData
    fun modifyPlugin(data: PluginData): PluginData
    fun archivePlugin(id: String, version: String): PluginData?

    fun getUserById(id: Long): UserData?
    fun getUserByOAuthData(user: SimplifiedUserAuthentication): UserData?
    fun createUserFromOAuthData(user: SimplifiedUserAuthentication, userData: UserUploadData): UserData
    fun modifyUserWithOAuthData(user: SimplifiedUserAuthentication, userData: UserUploadData): UserData
    fun deleteUserByOAuthData(user: SimplifiedUserAuthentication): UserData?

    fun getFile(uuid: String): FileDataTable.Entry

    fun getSettings(category: String): ValueMap
    fun storeSetting(category: String, key: String, value: TransferableValue): Boolean
}