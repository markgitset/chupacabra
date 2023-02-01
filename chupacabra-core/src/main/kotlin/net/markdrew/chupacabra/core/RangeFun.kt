package net.markdrew.chupacabra.core

/**
 * Returns true if, and only if, this range and [other] share one or more indices
 */
infix fun <T : Comparable<T>> ClosedRange<T>.intersects(other: ClosedRange<T>): Boolean =
    // true if both ranges start before the other one ends
    start <= other.endInclusive && other.start <= endInclusive

/**
 * Returns a new range consisting of the intersection of this range and [other]
 */
infix fun <T : Comparable<T>> ClosedRange<T>.intersect(other: ClosedRange<T>): ClosedRange<T> =
    max(start, other.start)..min(endInclusive, other.endInclusive)

private fun <T : Comparable<T>> min(x: T, y: T): T = if (x < y) x else y
private fun <T : Comparable<T>> max(x: T, y: T): T = if (x < y) y else x

/**
 * Returns a new range consisting of the union of this range and [other]
 */
//infix fun <T : Comparable<T>> ClosedRange<T>.enclose(other: ClosedRange<T>): ClosedRange<T> =
//    min(start, other.start)..max(endInclusive, other.endInclusive)

/**
 * Returns true if, and only if, every index in the given range, [r], is also in this range
 */
fun <T : Comparable<T>> ClosedRange<T>.encloses(r: ClosedRange<T>): Boolean =
    start <= r.start && endInclusive >= r.endInclusive

///**
// * Returns a list of 0, 1, or 2 non-empty [ClosedRange<T>]s that remain after removing [other] from this range.
// */
//operator fun <T : Comparable<T>> ClosedRange<T>.minus(other: ClosedRange<T>): List<ClosedRange<T>> {
//    val result = mutableListOf<ClosedRange<T>>()
//    val r1 = start until min(endInclusive + 1, other.start)
//    if (!r1.isEmpty()) result += r1
//    val r2 = max(start, other.endInclusive + 1)..endInclusive
//    if (!r2.isEmpty()) result += r2
//    return result
//}

//val rangestartendInclusiveComparator: Comparator<ClosedRange<T>> by lazy { compareBy<ClosedRange<T>>({ it.start }, { it.endInclusive }) }
