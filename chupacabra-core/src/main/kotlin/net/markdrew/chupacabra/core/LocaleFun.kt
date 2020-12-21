package net.markdrew.chupacabra.core

import java.util.Locale

/**
 * Looks up a (language-only) [Locale] by its display name.  If not found, returns null.
 */
fun localeForLanguage(languageDisplayName: String): Locale? =
    Locale.getISOLanguages().map { Locale.forLanguageTag(it) }
        .firstOrNull { it.displayLanguage.equals(languageDisplayName, ignoreCase = true) }

