package net.markdrew.chupacabra.core

import java.util.NavigableMap
import java.util.TreeMap
import kotlin.collections.MutableMap.MutableEntry

/**
 * A [NavigableMap] with disjoint (non-overlapping) [IntRange] keys. The primary purpose of this set is to provide some
 * well-tested methods (e.g., [enclosedBy], [intersectedBy], etc.) that make operations with another [IntRange] both
 * easy and much more efficient than a naive implementation not using this class.  Note that this map does not allow
 * null values.
 */
class DisjointRangeMap<T : Any>(vararg pairs: Pair<IntRange, T>) : TreeMap<IntRange, T>(RANGE_COMPARATOR) {

    init {
        putAll(pairs)
    }

    /**
     * @param m map from which to initialize this [DisjointRangeMap]
     */
    constructor(m: Map<out IntRange, T>) : this() {
        putAll(m)
    }

    /**
     * Puts the given [IntRange] key and value into this map.
     *
     * @param key the key [IntRange]
     * @param value the value
     * @return null
     * @throws IllegalArgumentException if the given [IntRange] key is empty
     * @throws IllegalStateException if the given [IntRange] key is not already present and it intersects any [IntRange]s
     *             that are already in this set
     */
    override fun put(key: IntRange, value: T): T? {
        if (key.isEmpty()) throw IllegalArgumentException("Empty range keys are not allowed")
        if (!containsKey(key)) {
            val intersections = internalIntersectedBy(key)
            if (!intersections.isEmpty()) {
                throw IllegalStateException(
                        "Attempted to add range $key ($value) but it intersects already present ranges $intersections")
            }
        }
        return super.put(key, value)
    }

    /**
     * Puts the given [IntRange] key and value into this map, removing (and returning) any keys that intersect with the given one.
     *
     * @param key the key [IntRange]
     * @param value the value
     * @return a [DisjointRangeMap] containing any entries that were removed to make room for the new entry
     * @throws IllegalArgumentException if the given [IntRange] key is empty
     * @throws IllegalStateException if the given [IntRange] key is not already present and it intersects any [IntRange]s
     *             that are already in this set
     */
    fun putForcefully(key: IntRange, value: T): DisjointRangeMap<T> {
        if (key.isEmpty()) throw IllegalArgumentException("Empty range keys are not allowed")
        val displacedEntries = intersectedBy(key)
        for (k in displacedEntries.keys) remove(k)
        put(key, value)
        return displacedEntries
    }

    /**
     * Puts each entry of the given [Map] into this map.
     *
     * @param from the [IntRange]-keyed map to add to this map
     * @throws IllegalArgumentException if any of the given [IntRange] keys is empty
     * @throws IllegalStateException if any of the given [IntRange] keys intersects any [IntRange] keys already in this set
     */
    override fun putAll(from: Map<out IntRange, T>) {
        from.forEach { (r, value) -> this[r] = value }
    }

    /**
     * For each entry from the given map whose key [IntRange] doesn't intersect any already-present key [IntRange]s 
     * (and which is not empty), puts the entry into this [DisjointRangeMap].
     */
    fun putAllNonIntersecting(from: Map<out IntRange, T>) {
        from.forEach { (r, value) ->
            if (!r.isEmpty() && internalIntersectedBy(r).isEmpty()) this[r] = value 
        }
    }

    override fun putIfAbsent(key: IntRange, value: T): T? {
        val prevValue = get(key)
        return if (prevValue == null) {
            if (intersects(key)) {
                throw IllegalStateException("Given key intersects, but does not equal an already-present key")
            }
            put(key, value)
            null
        } else prevValue
    }

    fun keyContaining(position: Int): IntRange? = entryContaining(position)?.key

    /**
     * Efficiently finds the range in this set that contains the given position, if one exists
     *
     * @param position the offset for which to find a containing range
     * @return the one range in this set for which [IntRange.contains] returns true, if it is present
     */
    fun valueContaining(position: Int): T? = entryContaining(position)?.value

    fun entryContaining(position: Int): MutableEntry<IntRange, T>? =
            floorEntry(eternalRange(position))?.takeIf { entry -> entry.key.last >= position }

    private fun internalEnclosedBy(r: IntRange): NavigableMap<IntRange, T> {

        // get the first range that begins at/after r begins (may be null)
        val startKey: IntRange? = ceilingKey(emptyRange(r.first))

        // if there are no ranges that begin at/after r begins OR the first such range ends beyond the end of r, return empty
        if (startKey == null || startKey.last > r.last) return TreeMap()

        // get the last range that begins before r ends (should never be null at this point)
        var endKey = floorKey(emptyRange(r.last + 1))

        // if the last candidate range ends after r ends, select the previous range (should never be null at this point)
        if (endKey.last > r.last) endKey = lowerKey(endKey)

        return subMap(startKey, true, endKey, true)
    }

    /**
     * Efficiently finds the set of ranges in this set that are enclosed by the given range. More specifically, returns a
     * remove-only, live view of this set that only includes those ranges in this set for which `r.encloses(t) ==
     * true`, where t is each range in this set.
     *
     * @param r the range which each of the resulting ranges must be enclosed by
     * @return the set of ranges in this set that are enclosed by the given range
     */
    fun enclosedBy(r: IntRange): DisjointRangeMap<T> = DisjointRangeMap(internalEnclosedBy(r))

    /**
     * True if and only if a single key range in this [DisjointRangeMap] encloses the given non-empty [IntRange].  Note that this
     * means that two adjacent ranges in this map whose union encloses the given range is not sufficient for this function to
     * return true. For example, { [1..5], [6..10] } encloses [3..5], but does NOT enclose [3..6].
     */
    fun encloses(r: IntRange): Boolean = entryEnclosing(r) != null

    /**
     * Finds and returns the value whose key encloses the given range, or null if no such entry exists in this map.
     */
    fun valueEnclosing(r: IntRange): T? = entryEnclosing(r)?.value

    /**
     * Finds and returns the key that encloses the given range, or null if no such entry exists in this map.
     */
    fun keyEnclosing(r: IntRange): IntRange? = entryEnclosing(r)?.key

    /**
     * Finds and returns the entry whose key encloses the given range, or null if no such entry exists in this map.
     */
    fun entryEnclosing(r: IntRange): MutableEntry<IntRange, T>? =
        if (r.isEmpty()) null
        else entryContaining(r.first)?.let { if (it.key.contains(r.last)) it else null }

    /**
     * Efficiently finds the set of range keys in this map that are enclosed by the given range. More specifically,
     * returns an immutable view of the values in this map whose keys are entirely enclosed within the given range
     * (i.e., for which `r.encloses(t) == true`, where t is each range key in this set).
     *
     * @param r the range in which each of the resulting values must be enclosed
     * @return an immutable list of the values in this map that are enclosed by the given range
     */
    fun valuesEnclosedBy(r: IntRange): List<T> = internalEnclosedBy(r).values.toList()

    private fun internalIntersectedBy(r: IntRange): NavigableMap<IntRange, T> {
        if (r.isEmpty()) return TreeMap()
        
        // get the last range that begins before r ends (may be null)
        val endKey: IntRange? = floorKey(emptyRange(r.last + 1))

        // if there are no ranges that begin before r ends OR the last such range ends before r starts, return empty
        if (endKey == null || endKey.last < r.first) return TreeMap()

        // get the last range that begins at/before r begins (may be null)
        var startKey: IntRange? = floorKey(r)

        if (startKey == null) {
            // if no range begins at/before r begins, start with the first range
            startKey = firstKey()
        } else {
            // if the first candidate range ends before r begins, select the next range
            if (startKey.last < r.first) startKey = higherKey(startKey)
        }

        return subMap(startKey, true, endKey, true)
    }

    /**
     * Efficiently finds the set of ranges in this set that are intersected by the given range. More specifically, returns a
     * remove-only, live view of this set that only includes those ranges in this set for which `r.intersects(t) ==
     * true`, where t is each range in this set.
     *
     * @param r the range which each of the resulting ranges must intersect
     * @return the set of ranges in this set that are intersected by the given range
     */
    fun intersectedBy(r: IntRange): DisjointRangeMap<T> = DisjointRangeMap(
            internalIntersectedBy(r)
    )

    /**
     * Returns true if the given range intersects any of the key ranges in this map
     */
    fun intersects(r: IntRange): Boolean = internalIntersectedBy(r).isNotEmpty()

    /**
     * Retrieve a list of values whose ranges are intersected by the given [r]
     */
    fun valuesIntersectedBy(r: IntRange): List<T> = internalIntersectedBy(r).values.toList()

    /**
     * Builds a new [DisjointRangeMap] that is a copy of this one, except for those entries that intersect any key
     * ranges in the given [DisjointRangeMap].  In other words, it returns this map minus the given map.
     *
     * @param subtrahend the map to subtract from this one
     * @return a copy of this map, but without any entries that intersect any key ranges of the given subtrahend map
     */
    fun <U : Any> minusIntersectedBy(subtrahend: DisjointRangeMap<U>): DisjointRangeMap<T> {
        val result = DisjointRangeMap<T>()
        for ((key, value) in entries) {
            if (subtrahend.internalIntersectedBy(key).isEmpty()) {
                result[key] = value
            }
        }
        return result
    }

    /**
     * Builds a new [DisjointRangeMap] that is a copy of this one, except for those entries that are enclosed by any
     * key ranges in the given [DisjointRangeMap].  In other words, it returns this map minus the given map.
     *
     * @param subtrahend the map to subtract from this one
     * @return a copy of this map, but without any entries that are enclosed by any key ranges of the given subtrahend
     * map
     */
    fun <U : Any> minusEnclosedBy(subtrahend: DisjointRangeMap<U>): DisjointRangeMap<T> {
        val result = DisjointRangeMap<T>()
        for ((key, value) in entries) {
            if (!subtrahend.encloses(key)) {
                result[key] = value
            }
        }
        return result
    }

    /**
     * Builds a new [DisjointRangeMap] that is a copy of this one, except for those entries that are enclosed by any
     * ranges in the given [DisjointRangeSet].  In other words, it returns this map minus the given set of ranges.
     *
     * @param subtrahend the set of ranges to subtract from this map
     * @return a copy of this map, but without any entries that are enclosed by any ranges of the given set
     */
    fun minusEnclosedBy(subtrahend: DisjointRangeSet): DisjointRangeMap<T> = minusEnclosedBy(subtrahend.map)

    /**
     * Removes the given range from this map, modifying any contained ranges that intersect the given range as needed.
     * A couple of illustrative examples:
     * <pre>
     * { [0,4], [6,10], [12,16], [18,22], [24,28] }  -  [7,20]  =  { [0,4], [6,6], [21,22], [24,28] }
     *                                   { [0,28] }  -  [7,20]  =  { [0,6], [21,28] }
     * </pre>
     * Note that this method does NOT modify this map.
     *
     * @param subtrahend the range to subtract/remove from this map
     */
    operator fun minus(subtrahend: IntRange): DisjointRangeMap<T> =
        DisjointRangeMap(this).apply { minusAssign(subtrahend) }

    /**
     * Removes the given range from this map, modifying any contained ranges that intersect the given range as needed.
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
        val intersectedBy: DisjointRangeMap<T> = intersectedBy(subtrahend)
        for (r: IntRange in intersectedBy.keys) {
            val removed: T? = remove(r)
            if (removed == null || subtrahend.encloses(r)) continue
            if (r.first < subtrahend.first) {
                put(IntRange(r.first, subtrahend.first - 1), removed)
            }
            if (r.last > subtrahend.last) {
                put(IntRange(subtrahend.last + 1, r.last), removed)
            }
        }
    }

    /**
     * Builds a new [DisjointRangeMap] from this one that only contains key ranges (or parts of ranges) that are
     * enclosed by key ranges in the given [mask].  I.e., any ranges already enclosed by a range in [mask] will be
     * retained as-is, any ranges intersecting a range in [mask] will be truncated/split to the part(s) that intersects
     * (if split, both keys will get the same value), and any other ranges will be dropped.
     */
    fun maskedBy(mask: DisjointRangeSet): DisjointRangeMap<T> = entries.flatMap { (k, v) ->
        // for each entry, yield 0 or more entries by intersecting it with ranges in the mask
        mask.rangesIntersectedBy(k).map { maskRange -> maskRange.intersect(k) to v }
    }.fold(DisjointRangeMap()) { result, (k, v) -> 
        // build the result
        result.apply { put(k, v) } 
    }

    /**
     * Builds a new [DisjointRangeMap] that combines this one with another one by splitting any non-identical,
     * intersecting ranges (greatest common divisor, or GCD). The values of the resulting map will contain a [Pair] of
     * the values from the two source maps (this map's values are first, and the other map's values are second).  Either
     * item in the [Pair] value may be null (if the range only occurred in one map), but both [Pair] values will never
     * be null.
     */
    fun <U : Any> gcdAlignment(other: DisjointRangeMap<U>): DisjointRangeMap<Pair<T?, U?>> = gcdAlignment(other, ::Pair)

//    fun disjointRangeSetView(): DisjointRangeSet = DisjointRangeSet(this, navigableKeySet())

    /**
     * Builds a new [DisjointRangeMap] that combines this one with another one by splitting any non-identical,
     * intersecting ranges (greatest common divisor, or GCD). The values of the resulting map will contain a [Pair] of
     * the values from the two source maps (this map's values are first, and the other map's values are second).  Either
     * item in the [Pair] value may be null (if the range only occurred in one map), but both [Pair] values will never
     * be null.
     */
    fun <U : Any, V : Any> gcdAlignment(other: DisjointRangeMap<out U>,
                                        valueTransform: (T?, U?) -> V): DisjointRangeMap<V> {

        val result: DisjointRangeMap<V> = DisjointRangeMap()

        // first pass handles intersections and leftovers from the other map
        for ((otherRange, otherValue) in other.entries) {

            var remainder = listOf(otherRange)

            // get this map's entries intersected by the other map's key
            for ((thisRange, thisValue) in internalIntersectedBy(otherRange)) {
                val newRange: IntRange = thisRange intersect otherRange
                result[newRange] = valueTransform(thisValue, otherValue)
                remainder = remainder.flatMap { it - newRange }
            }

            for (leftoverRange in remainder) {
                result[leftoverRange] = valueTransform(null, otherValue)
            }
                
        }

        // second pass only handles leftovers from this map
        for ((thisRange, thisValue) in entries) {

            var remainder = listOf(thisRange)

            // get other map's entries intersected by this map's key
            for ((otherRange, _) in other.internalIntersectedBy(thisRange)) {
                val newRange: IntRange = thisRange intersect otherRange
                // don't need to do the intersection here since it was handled in the first pass
                remainder = remainder.flatMap { it - newRange }
            }

            for (leftoverRange in remainder) {
                result[leftoverRange] = valueTransform(thisValue, null)
            }

        }
        return result
    }

    /**
     * Returns the [IntRange] that just contains all the key ranges of this map, or [IntRange.EMPTY] if this map is empty.
     */
    fun boundingRange(): IntRange =
        if (isEmpty()) IntRange.EMPTY
        else firstKey().first..lastKey().last

    // TODO figure out how to make this return a live view
//    override val keys: DisjointRangeSet
//        get() = DisjointRangeSet(super.keys)

    companion object {

        private val RANGE_COMPARATOR: Comparator<IntRange> =
                Comparator.comparingInt(IntRange::first).thenComparingInt(IntRange::last)

        /**
         * An IntRange that starts at the given position and is less than all other Ranges that start at the same position.
         *
         * Use this for CEILING operations when you want to INCLUDE ranges that start at the given position OR
         * for FLOOR operations when you want to EXCLUDE ranges that start at the given position.
         */
        private fun emptyRange(position: Int): IntRange {
            return IntRange(position, position - 1)
        }

        /**
         * An IntRange that starts at the given position and is greater than all other Ranges that start at the same position.
         *
         * Use this for FLOOR operations when you want to INCLUDE ranges that start at the given position OR
         * for CEILING operations when you want to EXCLUDE ranges that start at the given position.
         */
        private fun eternalRange(position: Int): IntRange {
            return IntRange(position, Integer.MAX_VALUE)
        }
    }

}

fun <T : Any> Sequence<Pair<IntRange, T>>.toDisjointRangeMap(): DisjointRangeMap<T> =
    DisjointRangeMap<T>().also { it += this }