package net.markdrew.chupacabra.core

/**
 * Just returns a typed identity function
 */
fun <T> identityFun(): (T) -> T = { it }