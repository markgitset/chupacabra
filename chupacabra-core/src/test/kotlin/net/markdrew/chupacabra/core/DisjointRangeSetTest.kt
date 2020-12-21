package net.markdrew.chupacabra.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.NoSuchElementException
import kotlin.Int.Companion.MAX_VALUE
import kotlin.Int.Companion.MIN_VALUE

class DisjointRangeSetTest {

    @Test
    fun testBasic() {
        val drs = DisjointRangeSet(4..4, 0..2, 11..12, 3..3, 8..9)

        assertEquals(0..2, drs.first())
        assertEquals(11..12, drs.last())
        assertEquals("[0..2, 3..3, 4..4, 8..9, 11..12]", drs.toString())
        assertEquals(5, drs.size)
        drs.clear()
        assertEquals(0, drs.size)
    }

    @Test
    fun testComparator() {
        val drs = DisjointRangeSet(4..4, 0..2, 11..12, 3..3, 8..9)
        val comp = drs.comparator()
        assertTrue(comp.compare(0..2, 3..3) < 0)
        assertTrue(comp.compare(3..3, 0..2) > 0)
        assertTrue(comp.compare(0..2, 0..2) == 0)
        assertTrue(comp.compare(0..29, 10..19) < 0)
        assertTrue(comp.compare(10..19, 0..29) > 0)
    }

    @Test
    fun testAddRange() {

        val rangeSet = DisjointRangeSet(4..4, 0..2, 11..12, 3..3, 8..9)
        assertEquals(5, rangeSet.size)

        assertTrue(rangeSet.add(13..14))
        assertEquals(6, rangeSet.size)

        rangeSet.add(13..14)
        assertEquals(6, rangeSet.size)

        assertThrows(IllegalStateException::class.java) { rangeSet.add(14..15) }

    }

    @Test
    fun testAddEmptyRange() {
        assertThrows(IllegalArgumentException::class.java) {
            DisjointRangeSet(4..4, 0..2, 11..12, IntRange.EMPTY, 3..3, 8..9)
        }
    }

    @Test
    fun testNonDisjointAdd1() {
        val drs = DisjointRangeSet(4..4, 0..2, 11..12, 3..3, 8..9)
        assertThrows(IllegalStateException::class.java) { drs.add(5..8) }
    }

    @Test
    fun testNonDisjointAdd2() {
        val drs = DisjointRangeSet(4..4, 0..2, 11..12, 3..3, 8..9)
        assertThrows(IllegalStateException::class.java) { drs.add(3..13) }
    }

    @Test
    fun testIterator() {
        val it = DisjointRangeSet(4..4, 0..2, 11..12, 3..3, 8..9).iterator()

        assertEquals(0..2, it.next())
        assertEquals(3..3, it.next())
        assertEquals(4..4, it.next())
        assertEquals(8..9, it.next())
        assertEquals(11..12, it.next())
        assertFalse(it.hasNext())
    }

    @Test
    fun testFirstInEmptySet() {
        assertThrows(NoSuchElementException::class.java) { DisjointRangeSet().first() }
    }

    @Test
    fun testLastInEmptySet() {
        assertThrows(NoSuchElementException::class.java) { DisjointRangeSet().last() }
    }

    @Test
    fun testEquals() {
        val set = DisjointRangeSet(0..2, 4..4, 11..12, 3..3, 8..9)
        val equalSet = DisjointRangeSet(0..2, 4..4, 11..12, 8..9, 3..3)
        val notEqualSet = DisjointRangeSet(0..2, 4..4, 3..3, 8..9)

        assertTrue(set == set)
        assertTrue(set == equalSet)
        assertTrue(equalSet == set)
        assertFalse(set == notEqualSet)
        assertFalse(notEqualSet == set)
    }

    @Test
    fun testHashCode() {
        val set = DisjointRangeSet(0..2, 4..4, 11..12, 3..3, 8..9)
        val equalSet = DisjointRangeSet(0..2, 4..4, 11..12, 8..9, 3..3)
        val notEqualSet = DisjointRangeSet(0..2, 4..4, 3..3, 8..9)

        assertTrue(set.hashCode() == equalSet.hashCode())
        assertTrue(equalSet.hashCode() == set.hashCode())
        assertFalse(set.hashCode() == notEqualSet.hashCode())
        assertFalse(notEqualSet.hashCode() == set.hashCode())
    }

    @Test
    fun testRangeContaining() {
        val drs = DisjointRangeSet(0..2, 3..3, 4..4, 8..9, 11..12)

        assertEquals(0..2, drs.rangeContaining(1))
        assertTrue((0..2).contains(1))

        assertEquals(4..4, drs.rangeContaining(4))
        assertTrue((4..4).contains(4))

        assertEquals(8..9, drs.rangeContaining(8))
        assertTrue((8..9).contains(8))

        assertNull(drs.rangeContaining(13))
    }

    @Test
    fun testRangesEnclosedBy() {
        val drs = DisjointRangeSet(3..3, 4..4, 8..9, 11..12)

        assertEquals(DisjointRangeSet(), drs.rangesEnclosedBy(0..0))
        assertEquals(DisjointRangeSet(3..3), drs.rangesEnclosedBy(0..3))
        assertEquals(DisjointRangeSet(3..3, 4..4, 8..9, 11..12), drs.rangesEnclosedBy(0..13))
        assertEquals(DisjointRangeSet(8..9), drs.rangesEnclosedBy(5..11))
        assertEquals(DisjointRangeSet(), drs.rangesEnclosedBy(9..11))
        assertEquals(DisjointRangeSet(8..9, 4..4), drs.rangesEnclosedBy(4..9))
        assertEquals(DisjointRangeSet(), drs.rangesEnclosedBy(14..19))

        // verify that the result is NOT a live view
        val view = drs.rangesEnclosedBy(4..9)
        assertTrue(view.remove(8..9))
        assertTrue(drs.contains(8..9))
        assertEquals(DisjointRangeSet(4..4), view)
    }

    @Test
    fun testRangesIntersectedBy() {
        val drs = DisjointRangeSet(3..3, 4..4, 8..9, 11..12)

        assertEquals(DisjointRangeSet(), drs.rangesIntersectedBy(0..0))
        assertEquals(DisjointRangeSet(3..3), drs.rangesIntersectedBy(0..3))
        assertEquals(DisjointRangeSet(3..3, 4..4, 8..9, 11..12), drs.rangesIntersectedBy(0..13))
        assertEquals(DisjointRangeSet(8..9, 11..12), drs.rangesIntersectedBy(5..11))
        assertEquals(DisjointRangeSet(8..9, 11..12), drs.rangesIntersectedBy(9..11))
        assertEquals(DisjointRangeSet(4..4, 8..9), drs.rangesIntersectedBy(4..9))
        assertEquals(DisjointRangeSet(), drs.rangesIntersectedBy(14..19))

        // verify that the result is NOT a live view
        val view = drs.rangesIntersectedBy(4..9)
        assertTrue(view.remove(8..9))
        assertTrue(drs.contains(8..9))
        assertEquals(DisjointRangeSet(4..4), view)
    }

    @Test
    fun testThatCopyConstructorIsNotALiveView() {
        val drs1 = DisjointRangeSet(3..3, 4..4, 8..9, 11..12)
        val drs2 = DisjointRangeSet(drs1)

        // at first, they should be equal
        assertTrue(drs1 == drs2)
        assertTrue(drs2 == drs1)

        // remove d from drs1 and verify that drs2 still has d
        assertTrue(drs1.remove(8..9))
        assertTrue(drs2.contains(8..9))

        // now, they should NOT be equal
        assertFalse(drs1 == drs2)
        assertFalse(drs2 == drs1)
    }

    @Test
    fun testInvert() {
        // { [0,4], [8,100] }.invert() = { [MIN_VALUE,-1], [5,7], [101,MAX_VALUE] }
        assertEquals(
            DisjointRangeSet(MIN_VALUE..-1, 5..7, 101..MAX_VALUE),
            DisjointRangeSet(0..4, 8..100).invert()
        )

        // { [0,4], [8,100] }.invert([-100,200]) = { [-100,-1], [5,7], [101,200] }
        assertEquals(
            DisjointRangeSet(-100..-1, 5..7, 101..200),
            DisjointRangeSet(0..4, 8..100).invert(-100..200)
        )

        // { [0,4], [6,10], [14,16], [18,20], [26,28] }.invert([7,23]) = { [11,13], [17,17], [21,23] }
        assertEquals(
            DisjointRangeSet(11..13, 17..17, 21..23),
            DisjointRangeSet(0..4, 6..10, 14..16, 18..20, 26..28).invert(7..23)
        )
    }

}
