package net.markdrew.chupacabra.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class RangeFunKtTest {

    @Test
    fun intersects() {
        assertTrue(1..5 intersects 3..7)
        assertTrue(3..7 intersects 1..5)
        assertTrue(1..7 intersects 3..5)
        assertTrue(3..5 intersects 1..7)
        assertFalse(1..3 intersects 5..7)
        assertFalse(5..7 intersects 1..3)
        assertFalse(5..7 intersects IntRange.EMPTY)
        assertFalse(IntRange.EMPTY intersects 5..7)
    }

    @Test
    fun intersection() {
        assertEquals(3..5, 1..5 intersect 3..7)
        assertEquals(3..5, 3..7 intersect 1..5)
        assertEquals(3..5, 1..7 intersect 3..5)
        assertEquals(3..5, 3..5 intersect 1..7)
        assertEquals(IntRange.EMPTY, 1..3 intersect 5..7)
        assertEquals(IntRange.EMPTY, 5..7 intersect 1..3)
        assertEquals(IntRange.EMPTY, IntRange.EMPTY intersect 1..3)
        assertEquals(IntRange.EMPTY, 5..7 intersect IntRange.EMPTY)
    }

    @Test
    fun minus() {
        assertEquals(listOf(1..2), (1..5) - (3..7))
        assertEquals(listOf(6..7), (3..7) - (1..5))
        assertEquals(listOf(1..2, 6..7), (1..7) - (3..5))
        assertEquals(emptyList<IntRange>(), (3..5) - (1..7))
        assertEquals(listOf(1..3), (1..3) - (5..7))
        assertEquals(listOf(5..7), (5..7) - (1..3))
        assertEquals(listOf(1..3), (1..3) - IntRange.EMPTY)
        assertEquals(emptyList<IntRange>(), IntRange.EMPTY - (1..3))
    }
    
    @Test
    fun shift() {
        assertEquals(4..7, (1..4).shift(3))
        assertEquals(-2..1, (1..4).shift(-3))
        assertEquals(7..6, (5..4).shift(2))
    }
    
    @Test
    fun length() {
        assertEquals(5, (1..5).length())
        assertEquals(1, (3..3).length())
        assertEquals(0, (5..2).length())
    }
}