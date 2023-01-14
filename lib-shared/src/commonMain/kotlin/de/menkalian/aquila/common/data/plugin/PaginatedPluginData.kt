package de.menkalian.aquila.common.data.plugin

data class PaginatedPluginData(
    val plugins: List<PluginData>,
    val start: Long,
    val amount: Long,
    val nextPage: String
)
