package net.markdrew.chupacabra.core

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import kotlin.math.sqrt

internal class StreamStatsTest {

    private val stats1 = sequenceOf(1.0, 2.0, 3.0, 4.0, 5.0).stats()
    private val stats2 = sequenceOf(1.0, 2.0, 2.0, 2.0, 2.0, 6.0).stats()
    private val stats3 = sequenceOf(1.0, 2.0, 2.0, 2.0).stats().update(sequenceOf(2.0, 6.0).stats())

    @Test
    fun getCount() {
        assertEquals(5, stats1.count)
        assertEquals(6, stats2.count)
        assertEquals(6, stats3.count)
    }

    @Test
    fun getMean() {
        assertEquals(3.0, stats1.mean, DELTA)
        assertEquals(15.0 / 6.0, stats2.mean, DELTA)
        assertEquals(15.0 / 6.0, stats3.mean, DELTA)
    }

    @Test
    fun getMin() {
        assertEquals(1.0, stats1.min, DELTA)
        assertEquals(1.0, stats2.min, DELTA)
        assertEquals(1.0, stats3.min, DELTA)
    }

    @Test
    fun getMax() {
        assertEquals(5.0, stats1.max, DELTA)
        assertEquals(6.0, stats2.max, DELTA)
        assertEquals(6.0, stats3.max, DELTA)
    }

    @Test
    fun sampleVariance() {
        assertEquals(2.5, stats1.sampleVariance(), DELTA)
        assertEquals(3.1, stats2.sampleVariance(), DELTA)
        assertEquals(3.1, stats3.sampleVariance(), DELTA)
    }

    @Test
    fun populationVariance() {
        assertEquals(2.0, stats1.populationVariance(), DELTA)
        assertEquals(31.0 / 12.0, stats2.populationVariance(), DELTA)
        assertEquals(31.0 / 12.0, stats3.populationVariance(), DELTA)
    }

    @Test
    fun sampleStdDev() {
        assertEquals(sqrt(2.5), stats1.sampleStdDev(), DELTA)
        assertEquals(sqrt(3.1), stats2.sampleStdDev(), DELTA)
        assertEquals(sqrt(3.1), stats3.sampleStdDev(), DELTA)
    }

    @Test
    fun populationStdDev() {
        assertEquals(sqrt(2.0), stats1.populationStdDev(), DELTA)
        assertEquals(sqrt(31.0 / 12.0), stats2.populationStdDev(), DELTA)
        assertEquals(sqrt(31.0 / 12.0), stats3.populationStdDev(), DELTA)
    }

    companion object {
        val DELTA = 0.000000001
    }
}