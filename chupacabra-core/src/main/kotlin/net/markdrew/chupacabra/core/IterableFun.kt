package net.markdrew.chupacabra.core

/**
 * Returns a List<T> in which runs of equal elements are squashed into a single element.  For example with [Int]s,
 * { 1, 2, 2, 3, 2, 2, 2, 4, 4, 5 } would become { 1, 2, 3, 2, 4, 5 }.
 *
 * The elements in the resulting list are in the same order as their runs in the source [Iterable].
 */
fun <T> Iterable<T>.squashRuns(): List<T> = squashRunsBy { it }

/**
 * Returns a List<T> in which runs of elements with equal keys (as determined by the given [selector] function)
 * are squashed into a single element.  For example with [Int]s, { 1, 2, 2, 3, 2, 2, 2, 4, 4, 5 } would become
 * { 1, 2, 3, 2, 4, 5 }.
 *
 * The elements in the resulting list are in the same order as their runs in the source [Iterable].
 */
fun <T, K> Iterable<T>.squashRunsBy(selector: (T) -> K): List<T> = squashRunsByTo(mutableListOf(), selector)

/**
 * Squashes runs of elements with equal keys (as determined by the given [selector] function) into a single element,
 * and adds them to the given collection.  For example with [Int]s, { 1, 2, 2, 3, 2, 2, 2, 4, 4, 5 } would add
 * { 1, 2, 3, 2, 4, 5 } to the given collection.
 *
 * The elements are added to the destination collection in the same order as their runs in the source [Iterable].
 */
fun <T, K, C : MutableCollection<in T>> Iterable<T>.squashRunsByTo(destination: C, selector: (T) -> K): C =
    destination.apply { addAll(this@squashRunsByTo.asSequence().squashRunsBy(selector)) }