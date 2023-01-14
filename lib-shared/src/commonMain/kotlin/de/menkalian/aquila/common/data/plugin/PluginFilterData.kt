package de.menkalian.aquila.common.data.plugin

data class PluginFilterData(
    val searchterm: String?,
    val creator: Long?,
    val tags: List<String>?,
    val timerange: TimeRangeData?
)
