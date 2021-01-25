package net.markdrew.chupacabra.core

import java.util.NavigableSet

/**
 * A [NavigableSet] of disjoint (non-overlapping) [IntRange]s. The primary purpose of this set is to provide some
 * well-tested methods (e.g., [rangesEnclosedBy], [rangesIntersectedBy], etc.) that make operations
 * with another [IntRange] both easy and much more efficient than a naive implementation not using this class.
 *
 * @param map the underlying map implementation
 * @param set a live view of the underlying map's keys to which most methods delegate
 */
class DisjointRangeSet internal constructor(
    internal val map: DisjointRangeMap<in Any>,
    private val set: NavigableSet<IntRange> = map.navigableKeySet(),
) : NavigableSet<IntRange> by set {

    /**
     * Constructs a new [DisjointRangeSet] from a [Collection] of [IntRange]s
     *
     * @param c collection of [IntRange]s
     */
    constructor(c: Collection<IntRange>) : this(DisjointRangeMap<Any>()) {
        addAll(c)
    }

    /**
     * Constructs a new [DisjointRangeSet] from the given [IntRange]s (or an empty set if no [IntRange]s are given)
     *
     * @param ranges (optional) [IntRange]s from which to construct a new [DisjointRangeSet]
     */
    constructor(vararg ranges: IntRange) : this(ranges.asList())

    /**
     * Adds the given [IntRange] to this set.
     *
     * @param element the [IntRange] to add to this set
     * @throws IllegalArgumentException if the given [IntRange] is empty
     * @throws IllegalStateException if the given [IntRange] is not already present and it intersects any [IntRange]s
     *              that are already in this set
     */
    override fun add(element: IntRange): Boolean {
        map[element] = Unit
        return true
    }

    /**
     * Adds the given [IntRange] to this set, removing (and returning) any ranges that intersect with added one.
     *
     * @param element the [IntRange] to add to this set
     * @throws IllegalArgumentException if the given [IntRange] is empty
     * @throws IllegalStateException if the given [IntRange] is not already present and it intersects any [IntRange]s
     *              that are already in this set
     */
    fun addForcefully(element: IntRange): DisjointRangeSet = DisjointRangeSet(
        map.putForcefully(element, Unit)
    )

    /**
     * Adds the given [IntRange]s to this set.
     *
     * @param elements the [IntRange]s to add to this set
     * @throws IllegalArgumentException if any of the given [IntRange]s is empty
     * @throws IllegalStateException if any of the given [IntRange]s intersects any [IntRange]s already in this set
     */
    override fun addAll(elements: Collection<IntRange>): Boolean {
        elements.forEach { add(it) }
        return true
    }

    /**
     * Efficiently finds the range in this set that contains the given position, if one exists
     *
     * @param position the offset for which to find a containing range
     * @return the one range in this set for which [IntRange.contains] returns true, if it is present
     */
    fun rangeContaining(position: Int): IntRange? = map.keyContaining(position)

    /**
     * Finds and returns the value which encloses the given range, or null if no such value exists in this set.
     */
    fun enclosing(range: IntRange): IntRange? = map.keyEnclosing(range)

    /**
     * Efficiently finds the subset of ranges that are enclosed by the given range. More specifically, returns a subset
     * of this set that only includes those ranges for which `r.encloses(t) == true`, where t is each range in this set.
     *
     * @param r the range which each of the resulting ranges must be enclosed by
     * @return the set of ranges in this set that are enclosed by the given range
     */
    fun rangesEnclosedBy(r: IntRange): DisjointRangeSet = DisjointRangeSet(map.enclosedBy(r))

    /**
     * Efficiently finds the subset of ranges that are intersected by the given range. More specifically, returns a
     * subset of [IntRange]s that only includes those ranges for which `r.intersects(t) == true`, where t is each range
     * in this set.
     *
     * @param r the range which each of the resulting ranges must intersect
     * @return the set of ranges in this set that are intersected by the given range
     */
    fun rangesIntersectedBy(r: IntRange): DisjointRangeSet = DisjointRangeSet(map.intersectedBy(r))

    /**
     * Removes the given range from this set, modifying any contained ranges that intersect the given range as needed.
     * A couple of illustrative examples:
     * <pre>
     * { [0,4], [6,10], [12,16], [18,22], [24,28] }  -  [7,20]  =  { [0,4], [6,6], [21,22], [24,28] }
     *                                   { [0,28] }  -  [7,20]  =  { [0,6], [21,28] }
     * </pre>
     * Note that this method modifies this set.
     *
     * @param subtrahend the range to subtract/remove from this set
     */
    operator fun minus(subtrahend: IntRange) {
        map.minus(subtrahend)
    }

    /**
     * Removes the given range from this set, modifying any contained ranges that intersect the given range as needed.
     * A couple of illustrative examples:
     * <pre>
     * { [0,4], [6,10], [12,16], [18,22], [24,28] }  -  [7,20]  =  { [0,4], [6,6], [21,22], [24,28] }
     *                                   { [0,28] }  -  [7,20]  =  { [0,6], [21,28] }
     * </pre>
     * Note that this method modifies this map.
     *
     * @param subtrahend the range to subtract/remove from this map
     */
    operator fun minusAssign(subtrahend: IntRange) {
        map.minusAssign(subtrahend)
    }

    /**
     * Builds a new [DisjointRangeSet] that is the inverse of this one, optionally constrained by the given
     * [boundingRange].  So, for example,
     * <pre>
     *     { [0,4], [8,100] }.invert() = { [MIN_VALUE,-1], [5,7], [101,MAX_VALUE] }
     *     { [0,4], [8,100] }.invert([-100,200]) = { [-100,-1], [5,7], [101,200] }
     *     { [0,4], [6,10], [14,16], [18,20], [26,28] }.invert([7,23]) = { [11,13], [17,17], [21,23] }
     * </pre>
     */
    fun invert(boundingRange: IntRange = Int.MIN_VALUE..Int.MAX_VALUE): DisjointRangeSet =
        DisjointRangeSet(boundingRange).apply { this@DisjointRangeSet.forEach { this -= it } }

    /**
     * Builds a new [DisjointRangeSet] from this one that only contains ranges (or parts of ranges) that are enclosed by
     * ranges in the given [mask].  I.e., any ranges already enclosed by a range in [mask] will be retained as-is, any
     * ranges intersecting a range in [mask] will be truncated to the part that intersects, and any other ranges will be
     * dropped.
     */
    fun maskedBy(mask: DisjointRangeSet): DisjointRangeSet = DisjointRangeSet(map.maskedBy(mask))

    /**
     * Builds a new [DisjointRangeMap] that combines this one with another one by splitting any non-identical,
     * intersecting ranges (greatest common divisor, or GCD). The values of the resulting map will come from the given
     * map if the key range was in the map.  Otherwise, the values will be [nullValue] (since [DisjointRangeMap]s don't
     * allow null values).
     */
    fun <U : Any> gcdAlignment(other: DisjointRangeMap<U>, nullValue: U): DisjointRangeMap<U> =
        map.gcdAlignment(other) { _, v -> v ?: nullValue }

    /**
     * Builds a new [DisjointRangeSet] that combines this one with another one by splitting any non-identical,
     * intersecting ranges (greatest common divisor, or GCD).
     */
    fun gcdAlignment(other: DisjointRangeSet): DisjointRangeSet =
        DisjointRangeSet(map.gcdAlignment(other.map) { _, _ -> })

    /**
     * Returns the [IntRange] that just contains all the ranges in this set, or [IntRange.EMPTY] if this set is empty.
     */
    fun boundingRange(): IntRange = map.boundingRange()

    /**
     * True if and only if a single range in this [DisjointRangeSet] encloses the given non-empty [IntRange].  Note that
     * this means that two adjacent ranges in this set whose union encloses the given range is not sufficient for this
     * function to return true. For example, { [1..5], [6..10] } encloses [3..5], but does NOT enclose [3..6].
     */
    fun encloses(r: IntRange): Boolean = map.entryEnclosing(r) != null

    /**
     * Efficiently finds the set of ranges in this set that are enclosed by the given range. More specifically, returns
     * a remove-only, live view of this set that only includes those ranges in this set for which `r.encloses(t) ==
     * true`, where t is each range in this set.
     *
     * @param r the range which each of the resulting ranges must be enclosed by
     * @return the set of ranges in this set that are enclosed by the given range
     */
    fun enclosedBy(r: IntRange): DisjointRangeSet = DisjointRangeSet(map.enclosedBy(r))

    override fun equals(other: Any?): Boolean = set == other

    override fun hashCode(): Int = set.hashCode()

    override fun toString(): String = set.toString()

}

fun Sequence<IntRange>.toDisjointRangeSet(): DisjointRangeSet = DisjointRangeSet().also { it += this }