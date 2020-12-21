package net.markdrew.chupacabra.core

/**
 * Returns the implementation version string from the manifest of the jar containing class T
 */
inline fun <reified T> manifestImplVersionOf(unknownVersion: String = "unknown"): String = 
    T::class.java.`package`?.implementationVersion ?: unknownVersion

/**
 * Returns the specification version string from the manifest of the jar containing class T
 */
inline fun <reified T> manifestSpecVersionOf(unknownVersion: String = "unknown"): String = 
    T::class.java.`package`?.specificationVersion ?: unknownVersion
