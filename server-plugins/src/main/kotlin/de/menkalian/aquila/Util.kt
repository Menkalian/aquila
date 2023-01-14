package de.menkalian.aquila

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Any.logger(): Logger {
    return LoggerFactory.getLogger(this::class.java)
}
