@file:Suppress("unused")

package de.menkalian.aquila.api

import kotlinx.serialization.Serializable

@Serializable
data class TransferableValue(val type: ConfigValueType, var value: String) {
    constructor(int: Int) : this(ConfigValueType.INT, int.toString())
    constructor(long: Long) : this(ConfigValueType.LONG, long.toString())
    constructor(boolean: Boolean) : this(ConfigValueType.BOOL, boolean.toString())
    constructor(float: Float) : this(ConfigValueType.FLOAT, float.toString())
    constructor(double: Double) : this(ConfigValueType.FLOAT, double.toString())
    constructor(string: String) : this(ConfigValueType.STRING, string)

    fun setValue(value: String): Boolean {
        try {
            this.value = when (type) {
                ConfigValueType.INT   -> value.toInt().toString()
                ConfigValueType.FLOAT -> value.toDouble().toString()
                ConfigValueType.BOOL  -> value.toBoolean().toString()
                else                  -> value
            }
            return true
        } catch (e: ClassCastException) {
            return false
        }
    }

    fun asString() = value
    fun asInt() = value.toInt()
    fun asLong() = value.toLong()
    fun asBool() = value.toBoolean()
    fun asFloat() = value.toFloat()
    fun asDouble() = value.toDouble()
}

enum class ConfigValueType {
    INT, LONG, FLOAT, STRING, BOOL
}
