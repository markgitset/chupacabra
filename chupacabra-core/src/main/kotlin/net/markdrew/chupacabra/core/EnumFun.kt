package net.markdrew.chupacabra.core

import java.util.Locale

/**
 * Returns the lower-cased name of this enum
 */
inline fun <reified T : Enum<T>> T.toLowerCase() = name.lowercase()

/**
 * Parses the given string as an enum.  By default, the parse is case-insensitive.  If the enum
 * can't be found, returns null.
 */
inline fun <reified T : Enum<T>> parseEnumOrNull(s: String?, caseSensitive: Boolean = false): T? =
    if (s == null) null
    else try {
        parseEnum<T>(s, caseSensitive)
    } catch (e: IllegalArgumentException) { null }

/**
 * Parses the given string as an enum.  By default, the parse is case-insensitive.  If the enum
 * can't be found, throws an [IllegalArgumentException].
 */
inline fun <reified T : Enum<T>> parseEnum(s: String, caseSensitive: Boolean = false): T =
    if (caseSensitive) enumValueOf(s)
    else enumValues<T>().firstOrNull { it.name.equals(s, ignoreCase = true) } 
            ?: throw IllegalArgumentException("No enum constant '$s'")
