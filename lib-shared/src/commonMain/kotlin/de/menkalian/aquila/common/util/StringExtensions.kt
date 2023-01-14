package de.menkalian.aquila.common.util

fun String.getFilenameExtension(): String? {
    return this.split(".").let {
        if (length > 1) {
            it.last()
        } else {
            null
        }
    }
}

fun String.getFilenameWithoutExtension(): String {
    return this.substring(0, this.lastIndexOf('.'))
}

fun String.insertIndex(i: Int): String {
    return this.replace("XXX", i.toString().padStart(3, '0'))
}
