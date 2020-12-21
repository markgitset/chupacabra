package net.markdrew.chupacabra.core

import java.io.File
import java.util.Properties

/**
 * Copies this [Properties] into a [MutableMap]<String, String>
 */
fun Properties.toStringMap(): LinkedHashMap<String, String> =
    // use LinkedHashMap for consistent property ordering
    stringPropertyNames().associateWithTo(linkedMapOf()) { getProperty(it) }

/**
 * Copies this [MutableMap]<String, String> into a [Properties]
 */
fun Map<String, String?>.toProperties(): Properties = Properties().apply { 
    this@toProperties.forEach { key, value -> if (value != null) setProperty(key, value) }
}

/**
 * Write this [MutableMap]<String, String> as a [Properties] to [file]
 */
fun Map<String, String?>.writeAsProperties(file: File, comments: String? = null) {
    this.toProperties().write(file, comments)
}

/**
 * Write this [Properties] to [file]
 */
fun Properties.write(file: File, comments: String? = null) {
    file.writer().use { w -> this.store(w, comments) }
}

/**
 * Read a new [Properties] from [file]
 */
fun readProperties(file: File): Properties = Properties().apply { file.reader().use { r -> load(r) } }

/**
 * Read a new [MutableMap]<String, String> from the given properties [file]
 */
fun readPropertiesAsMap(file: File): MutableMap<String, String> = readProperties(file).toStringMap()

/**
 * Interprets [propName]'s value as a [Boolean], using [default] when the property is absent 
 */
fun Properties.getBooleanProperty(propName: String, default: Boolean = false): Boolean =
    getProperty(propName)?.toBoolean() ?: default

/**
 * Interprets [propName]'s value as a [Boolean], using [default] when the property is absent
 */
fun Map<String, String>.getBooleanProperty(propName: String, default: Boolean = false): Boolean =
    get(propName)?.toBoolean() ?: default

/**
 * Interprets [propName]'s value as an [Int], using [default] when the property is absent 
 */
fun Properties.getIntProperty(propName: String, default: Int): Int =
    getProperty(propName)?.toInt() ?: default

/**
 * Interprets [propName]'s value as a [Int], using [default] when the property is absent
 */
fun Map<String, String>.getIntProperty(propName: String, default: Int): Int =
    get(propName)?.toInt() ?: default
