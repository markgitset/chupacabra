package net.markdrew.chupacabra.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import net.markdrew.chupacabra.core.Fraction.Companion.times
import net.markdrew.chupacabra.core.Fraction.Companion.weightedAverage

internal class FractionTest {

    @Test
    fun times() {
        assertEquals(Fraction(6, 12), Fraction(2, 3) * Fraction(3, 4))
        assertEquals(0.375, 0.5 * Fraction(3, 4))
        assertEquals(0.375, Fraction(3, 4) * 0.5)
    }

    @Test
    fun weightedAverage() {
        assertEquals(Fraction(5, 7), Fraction(2, 3).weightedAverage(Fraction(3, 4)))
        assertEquals(Fraction(11, 19), listOf(Fraction(6, 12), Fraction(2, 3), Fraction(3, 4)).weightedAverage())
    }

//    @Test
//    fun times1() {
//    }
//
//    @Test
//    fun plus() {
//    }
//
//    @Test
//    fun plus1() {
//    }
}