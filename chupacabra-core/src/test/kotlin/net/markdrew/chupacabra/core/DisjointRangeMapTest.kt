package net.markdrew.chupacabra.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DisjointRangeMapTest {

    companion object {

        private fun newRangeMap(range: IntRange, value: String): DisjointRangeMap<String> =
                DisjointRangeMap<String>().apply { put(range, value) }

    }

    // test values
    private val values: DisjointRangeMap<String> = DisjointRangeMap<String>().apply {
        put(4..4, "c")
        put(0..2, "a")
        put(11..12, "e")
        put(3..3, "b")
        put(8..9, "d")
    }

    private fun newRangeMap(vararg ranges: IntRange): DisjointRangeMap<String> {
        val map = DisjointRangeMap<String>()
        for (r in ranges) {
            values[r]?.let { map.put(r, it) }
        }
        return map
    }

    @Test
    fun testBasic() {
        assertEquals(0..2, values.firstKey())
        assertEquals(11..12, values.lastKey())
        assertEquals("{0..2=a, 3..3=b, 4..4=c, 8..9=d, 11..12=e}", values.toString())
        assertEquals(5, values.size)
        values.clear()
        assertEquals(0, values.size)
    }

    @Test
    fun testComparator() {
        val comp = values.comparator()
        assertTrue(comp.compare(0..2, 3..3) < 0)
        assertTrue(comp.compare(3..3, 0..2) > 0)
        assertTrue(comp.compare(0..2, 0..2) == 0)
        assertTrue(comp.compare(0..29, 10..19) < 0)
        assertTrue(comp.compare(10..19, 0..29) > 0)
    }

    @Test
    fun testAddItem() {

        // initial put
        assertNull(values.put(13..14, "x"))
        assertEquals("x", values[13..14])

        // put a new value with an existing key
        assertEquals("x", values.put(13..14, "z"))
        assertEquals("z", values[13..14])

        // put a new value with an intersecting key
        assertThrows(IllegalStateException::class.java) {
            values[14..15] = "y"
        }

    }

    @Test
    fun testPutIfAbsent() {

        // initial put
        assertNull(values.putIfAbsent(13..14, "x"))
        assertEquals("x", values[13..14])

        // put a new value with an existing key
        assertEquals("x", values.putIfAbsent(13..14, "z"))
        assertEquals("x", values[13..14])

        // put a new value with an intersecting key
        assertThrows(IllegalStateException::class.java) {
            values.putIfAbsent(14..15, "y")
        }

    }

    @Test
    fun testPutEmptyRange() {
        assertThrows(IllegalArgumentException::class.java) { values[IntRange.EMPTY] = "z" }
    }

    @Test
    fun testNonDisjointPut1() {
        assertThrows(IllegalStateException::class.java) { values[5..8] = "z" }
    }

    @Test
    fun testNonDisjointPut2() {
        assertThrows(IllegalStateException::class.java) { values[3..13] = "z" }
    }

    @Test
    fun testRemoveItem() {
        val r = 13..18
        assertFalse(values.containsKey(r))
        assertNull(values.remove(r))
        assertFalse(values.containsKey(r))

        assertTrue(values.containsKey(3..3))
        assertEquals("b", values.remove(3..3))
        assertFalse(values.containsKey(3..3))

    }

    @Test
    fun testReplaceItem() {
        val r = 13..18
        assertFalse(values.containsKey(r))
        assertNull(values.replace(r, "abc"))
        assertFalse(values.containsKey(r))

        assertEquals("b", values[3..3])
        assertEquals("b", values.replace(3..3, "abc"))
        assertEquals("abc", values[3..3])

    }

    @Test
    fun testRangeContaining() {
        assertEquals("a", values.valueContaining(1))
        assertTrue((0..2).contains(1))

        assertEquals("c", values.valueContaining(4))
        assertTrue((4..4).contains(4))

        assertEquals("d", values.valueContaining(8))
        assertTrue((8..9).contains(8))

        assertNull(values.valueContaining(13))
    }

    @Test
    fun testEnclosedBy() {
        val map = newRangeMap(3..3, 4..4, 8..9, 11..12)

        assertEquals(newRangeMap(), map.enclosedBy(0..0))
        assertEquals(newRangeMap(3..3), map.enclosedBy(0..3))
        assertEquals(newRangeMap(3..3, 4..4, 8..9, 11..12), map.enclosedBy(0..13))
        assertEquals(newRangeMap(8..9), map.enclosedBy(5..11))
        assertEquals(newRangeMap(), map.enclosedBy(9..11))
        assertEquals(newRangeMap(8..9, 4..4), map.enclosedBy(4..9))
        assertEquals(newRangeMap(), map.enclosedBy(14..19))
        assertEquals(newRangeMap(), newRangeMap(0..9, "abc").enclosedBy(3..6))
        assertEquals(
                newRangeMap(
                        3..6, "abc"
                ), newRangeMap(3..6, "abc").enclosedBy(0..9))

        // verify that the result is NOT a live view
        val view = map.enclosedBy(4..9)
        assertEquals("d", view.remove(8..9))
        assertEquals(newRangeMap(4..4), view)
        assertTrue(map.containsKey(8..9))
    }

    @Test
    fun testEncloses() {
        val map = DisjointRangeMap(1..3 to 1, 4..5 to 2, 8..9 to 3, 11..12 to 4)

        assertFalse(map.encloses(24..29))
        assertFalse(map.encloses(IntRange.EMPTY))
        assertFalse(map.encloses(5..4))
        assertTrue(map.encloses(1..2))
        assertTrue(map.encloses(4..4))
        assertTrue(map.encloses(8..9))
        assertTrue(map.encloses(2..3))
        assertFalse(map.encloses(3..4))
    }

    @Test
    fun testValuesIntersectedBy() {
        val map = DisjointRangeMap(1..3 to 1, 4..5 to 2, 8..9 to 3, 11..12 to 4)

        assertEquals(emptyList<Int>(), map.valuesIntersectedBy(24..29))
        assertEquals(emptyList<Int>(), map.valuesIntersectedBy(IntRange.EMPTY))
        assertEquals(emptyList<Int>(), map.valuesIntersectedBy(5..4))
        assertEquals(listOf(1), map.valuesIntersectedBy(1..2))
        assertEquals(listOf(2), map.valuesIntersectedBy(4..4))
        assertEquals(listOf(3), map.valuesIntersectedBy(8..9))
        assertEquals(listOf(1), map.valuesIntersectedBy(2..3))
        assertEquals(listOf(1, 2), map.valuesIntersectedBy(3..4))
        assertEquals(listOf(1, 2, 3), map.valuesIntersectedBy(3..8))
    }

    @Test
    fun testIntersects() {
        val map = DisjointRangeMap(1..3 to 1, 4..5 to 2, 8..9 to 3, 11..12 to 4)

        assertFalse(map.intersects(24..29))
        assertFalse(map.intersects(IntRange.EMPTY))
        assertFalse(map.intersects(5..4))
        assertTrue(map.intersects(1..2))
        assertTrue(map.intersects(4..4))
        assertTrue(map.intersects(8..9))
        assertTrue(map.intersects(2..3))
        assertTrue(map.intersects(3..4))
        assertTrue(map.intersects(3..8))
    }

    @Test
    fun testPutAllNonIntersecting() {
        val map = DisjointRangeMap(1..3 to 1, 4..5 to 2, 8..9 to 3, 11..12 to 4)
        
        val expected = DisjointRangeMap(1..3 to 1, 4..5 to 2, 8..9 to 3, 10..10 to 13, 11..12 to 4, 24..29 to 5)

        map.putAllNonIntersecting(mapOf(
                24..29 to 5,
                IntRange.EMPTY to 6,
                5..4 to 7,
                1..2 to 8,
                4..4 to 9,
                8..9 to 10,
                2..3 to 11,
                3..4 to 12,
                10..10 to 13,
                3..8 to 14
        ))
        
        assertEquals(expected, map)
    }

    @Test
    fun testValueEnclosing() {
        val map = DisjointRangeMap(1..3 to 1, 4..5 to 2, 8..9 to 3, 11..12 to 4)

        assertNull(map.valueEnclosing(24..29))
        assertNull(map.valueEnclosing(IntRange.EMPTY))
        assertNull(map.valueEnclosing(5..4))
        assertEquals(1, map.valueEnclosing(1..2))
        assertEquals(2, map.valueEnclosing(4..4))
        assertEquals(3, map.valueEnclosing(8..9))
        assertEquals(1, map.valueEnclosing(2..3))
        assertNull(map.valueEnclosing(3..4))
    }

    @Test
    fun testKeyEnclosing() {
        val map = DisjointRangeMap(1..3 to 1, 4..5 to 2, 8..9 to 3, 11..12 to 4)

        assertNull(map.keyEnclosing(24..29))
        assertNull(map.keyEnclosing(IntRange.EMPTY))
        assertNull(map.keyEnclosing(5..4))
        assertEquals(1..3, map.keyEnclosing(1..2))
        assertEquals(4..5, map.keyEnclosing(4..4))
        assertEquals(8..9, map.keyEnclosing(8..9))
        assertEquals(1..3, map.keyEnclosing(2..3))
        assertNull(map.keyEnclosing(3..4))
    }

    @Test
    fun testValuesEnclosedBy() {
        val map = newRangeMap(3..3, 4..4, 8..9, 11..12)

        assertEquals(emptyList<String>(), map.valuesEnclosedBy(0..0))
        assertEquals(listOf("b"), map.valuesEnclosedBy(0..3))
        assertEquals(listOf("b", "c", "d", "e"), map.valuesEnclosedBy(0..13))
        assertEquals(listOf("d"), map.valuesEnclosedBy(5..11))
        assertEquals(emptyList<String>(), map.valuesEnclosedBy(9..11))
        assertEquals(listOf("c", "d"), map.valuesEnclosedBy(4..9))
        assertEquals(emptyList<String>(), map.valuesEnclosedBy(14..19))
        assertEquals(emptyList<String>(), newRangeMap(0..9, "abc").valuesEnclosedBy(3..6))
        assertEquals(listOf("abc"), newRangeMap(3..6, "abc").valuesEnclosedBy(0..9))

    }

    @Test
    fun testIntersectedBy() {
        val map = newRangeMap(3..3, 4..4, 8..9, 11..12)
        assertEquals(newRangeMap(), map.intersectedBy(0..0))
        assertEquals(newRangeMap(3..3), map.intersectedBy(0..3))
        assertEquals(newRangeMap(3..3, 4..4, 8..9, 11..12), map.intersectedBy(0..13))
        assertEquals(newRangeMap(8..9, 11..12), map.intersectedBy(5..11))
        assertEquals(newRangeMap(8..9, 11..12), map.intersectedBy(9..11))
        assertEquals(newRangeMap(4..4, 8..9), map.intersectedBy(4..9))
        assertEquals(newRangeMap(), map.intersectedBy(14..19))
        assertEquals(
                newRangeMap(
                        0..9, "abc"
                ), newRangeMap(0..9, "abc").intersectedBy(3..6))
        assertEquals(
                newRangeMap(
                        3..6, "abc"
                ), newRangeMap(3..6, "abc").intersectedBy(0..9))
        assertEquals(
                newRangeMap(
                        0..9, "abc"
                ), newRangeMap(0..9, "abc").intersectedBy(0..6))
        assertEquals(
                newRangeMap(
                        3..6, "abc"
                ), newRangeMap(3..6, "abc").intersectedBy(3..9))

        // verify that the result is NOT a live view
        val view = map.intersectedBy(4..9)
        assertEquals("d", view.remove(8..9))
        assertEquals(newRangeMap(4..4), view)
        assertTrue(map.containsKey(8..9))
    }

    @Test
    fun testPutForcefully() {
        val original = newRangeMap(3..3, 4..4, 8..9, 11..12)

        testForcePut(original, 0..0, "x")
        testForcePut(original, 0..3, "x")
        testForcePut(original, 0..13, "x")
        testForcePut(original, 5..11, "x")
        testForcePut(original, 9..11, "x")
        testForcePut(original, 4..9, "x")
        testForcePut(original, 14..19, "x")

        testForcePut(newRangeMap(0..9, "abc"), 3..6, "x")
        testForcePut(newRangeMap(3..6, "abc"), 0..9, "x")
        testForcePut(newRangeMap(0..9, "abc"), 0..6, "x")
        testForcePut(newRangeMap(3..6, "abc"), 3..9, "x")
    }

    private fun testForcePut(original: DisjointRangeMap<String>, key: IntRange, value: String) {
        val map = DisjointRangeMap(original)
        val intersection = original.intersectedBy(key)
        val expected = DisjointRangeMap(original)
        intersection.forEach { (k, _) -> expected.remove(k) } 
        expected[key] = value
        assertEquals(intersection, map.putForcefully(key, value))
        assertEquals(expected, map)
    }

    @Test
    fun testMinusIntersectedBy() {
        val map = newRangeMap(0..2, 3..3, 4..4, 8..9, 11..12)
        val subtrahend = newRangeMap(3..3)
        subtrahend[9..13] = "abc"
        val expected = newRangeMap(0..2, 4..4)
        assertEquals(expected, map.minusIntersectedBy(subtrahend))
    }

    @Test
    fun testMaskedBy() {
        val map = DisjointRangeMap(0..2 to "a", 3..5 to "b", 6..6 to "c", 8..10 to "d", 12..14 to "e", 16..18 to "f")
        val mask = DisjointRangeSet(4..8, 9..13)
        val expected = DisjointRangeMap(4..5 to "b", 6..6 to "c", 8..8 to "d", 9..10 to "d", 12..13 to "e")
        assertEquals(expected, map.maskedBy(mask))
    }

    @Test
    fun testMinusAssign() {
        val map = DisjointRangeMap<String>()
        map[0..9] = "test1"
        map[11..12] = "test2"
        map[21..31] = "test3"
        map[32..41] = "test4"
        map[51..71] = "test5"
        map -= 12..34

        val expected = DisjointRangeMap<String>()
        expected[0..9] = "test1"
        expected[11..11] = "test2"
        expected[35..41] = "test4"
        expected[51..71] = "test5"

        assertEquals(expected, map)
    }

    @Test
    fun testMinusAssignEnclosedRange1() {
        val map = DisjointRangeMap<String>()
        map[0..9] = "test"
        map -= 2..7

        val expected = DisjointRangeMap<String>()
        expected[0..1] = "test"
        expected[8..9] = "test"

        assertEquals(expected, map)
    }

    @Test
    fun testMinusAssignEnclosedRange2() {
        val map = DisjointRangeMap<String>()
        map[2..7] = "test"
        map -= 0..9

        val expected = DisjointRangeMap<String>()

        assertEquals(expected, map)
    }

    @Test
    fun testMinusEnclosedBy() {
        val map1 = DisjointRangeMap(0..9 to "test", 13..16 to "a", 20..25 to "c", 26..30 to "e", 35..37 to "g")
        val map2 = DisjointRangeMap(0..9 to "test", 14..16 to "b", 20..23 to "d", 28..31 to "f")
        val expectedOneMinusTwo = DisjointRangeMap(13..16 to "a", 20..25 to "c", 26..30 to "e", 35..37 to "g")
        val expectedTwoMinusOne = DisjointRangeMap(28..31 to "f")

        assertEquals(expectedOneMinusTwo, map1.minusEnclosedBy(map2))
        assertEquals(expectedTwoMinusOne, map2.minusEnclosedBy(map1))
    }

    @Test
    fun testMinusEnclosedBySet() {
        val map = DisjointRangeMap(0..9 to "test", 13..16 to "a", 20..25 to "c", 26..30 to "e", 35..37 to "g")
        val set = DisjointRangeSet(0..9, 14..16, 20..23, 28..31)
        val expectedOneMinusTwo = DisjointRangeMap(13..16 to "a", 20..25 to "c", 26..30 to "e", 35..37 to "g")
        assertEquals(expectedOneMinusTwo, map.minusEnclosedBy(set))
    }

    @Test
    fun testGcdAlignment() {

        val first = DisjointRangeMap<String>().apply {
            this[1..1] = "a"
            this[2..10] = "b"
            this[11..11] = "c"
            this[12..17] = "d"
            this[20..27] = "e"
            this[30..32] = "f"
        }

        val second = DisjointRangeMap<Int>().apply {
            this[1..3] = 1
            this[5..6] = 2
            this[18..36] = 3
            this[40..41] = 4
        }

        val expected = DisjointRangeMap<Pair<String?, Int?>>().apply {
            this[1..1] = Pair("a", 1)
            this[2..3] = Pair("b", 1)
            this[4..4] = Pair("b", null)
            this[5..6] = Pair("b", 2)
            this[7..10] = Pair("b", null)
            this[11..11] = Pair("c", null)
            this[12..17] = Pair("d", null)
            this[18..19] = Pair(null, 3)
            this[20..27] = Pair("e", 3)
            this[28..29] = Pair(null, 3)
            this[30..32] = Pair("f", 3)
            this[33..36] = Pair(null, 3)
            this[40..41] = Pair(null, 4)
        }

        val firstActual: DisjointRangeMap<Pair<String?, Int?>> = first.gcdAlignment(second)
        //firstActual.asSequence().joinTo(System.out, "\n", postfix = "\n")
        assertEquals(expected, firstActual)

        // check that the operation is symmetric
        val reverseExpected: DisjointRangeMap<Pair<Int?, String?>> =
            expected.mapValuesTo(DisjointRangeMap()) { (_, v) -> Pair(v.second, v.first) }
        assertEquals(reverseExpected, second.gcdAlignment(first))
    }

    @Test
    fun testBoundingRange() {
        assertEquals(IntRange.EMPTY, DisjointRangeMap<String>().boundingRange())
        assertEquals(3..19, DisjointRangeMap(3..19 to "test").boundingRange())
        assertEquals(3..145, DisjointRangeMap(27..145 to "uh", 3..19 to "test").boundingRange())
    }
}
