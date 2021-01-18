@file:Suppress("unused")

package de.menkalian.aquila.util

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

internal fun File.md5(): String {
    return hashed("MD5")
}

internal fun File.sha256(): String {
    return hashed("SHA-256")
}

internal fun File.sha512(): String {
    return hashed("SHA-512")
}

internal fun File.hashed(algo: String): String {
    val bytes = this.readBytes()
    val md = MessageDigest.getInstance(algo)
    val digest = md.digest(bytes)
    return digest.fold("", { str, it -> str + "%02x".format(it) })
}

internal fun File.unzip(target: File) {
    if (!target.exists()) {
        target.mkdir()
    }

    assert(target.isDirectory)

    val buffer = ByteArray(1024)

    val inputStream = ZipInputStream(this.inputStream())

    try {
        while (true) {
            val entry: ZipEntry = inputStream.nextEntry ?: break
            val fileName = entry.name
            val entryFile = File(target.absolutePath + File.separator + fileName)

            FileOutputStream(entryFile).use { out ->
                var len: Int
                while (run {
                        len = inputStream.read(buffer)
                        len
                    } > 0) {
                    out.write(buffer, 0, len)
                }
            }
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
}

class InvalidChecksumException : RuntimeException("File could not be verified")