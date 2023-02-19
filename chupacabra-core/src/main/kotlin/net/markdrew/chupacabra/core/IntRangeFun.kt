package net.markdrew.chupacabra.core

import kotlin.math.max
import kotlin.math.min

/**
 * Number of indices in this range, or zero if this is an empty range
 */
fun IntRange.length(): Int = max(last - first + 1, 0)

/**
 * Builds a new range of the same length as this one, but with the indices shifted by [offset]
 */
fun IntRange.shift(offset: Int): IntRange = first + offset .. last + offset

/**
 * Returns true if, and only if, this range and [other] share one or more indices
 */
infix fun IntRange.intersects(other: IntRange): Boolean =
    // true if both ranges start before the other one ends
    first <= other.last && other.first <= last

/**
 * Returns a new range consisting of the intersection of this range and [other]
 */
infix fun IntRange.intersect(other: IntRange): IntRange =
    max(first, other.first)..min(last, other.last)

/**
 * Returns a new range consisting of the union of this range and [other]
 */
infix fun IntRange.enclose(other: IntRange): IntRange =
    min(first, other.first)..max(last, other.last)

/**
 * Returns true if, and only if, every index in the given range, [r], is also in this range
 */
fun IntRange.encloses(r: IntRange): Boolean = first <= r.first && last >= r.last

/**
 * Returns a list of 0, 1, or 2 non-empty [IntRange]s that remain after removing [other] from this range.
 */
operator fun IntRange.minus(other: IntRange): List<IntRange> {
    val result = mutableListOf<IntRange>()
    val r1 = first until min(last + 1, other.first)
    if (!r1.isEmpty()) result += r1
    val r2 = max(first, other.last + 1)..last
    if (!r2.isEmpty()) result += r2
    return result
}

fun IntRange.splitBefore(splitOffset: Int): Pair<IntRange, IntRange> {
    require(splitOffset in (first + 1)..last) { "Can't split range $this before offset $splitOffset" }
    return first until splitOffset to splitOffset..last
}

val rangeFirstLastComparator: Comparator<IntRange> by lazy { compareBy<IntRange>({ it.first }, { it.last }) }

fun ClosedRange<*>.toMathString(): String = "[$start, $endInclusive]"