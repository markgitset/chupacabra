package net.markdrew.chupacabra.core

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import kotlin.Double.Companion.MAX_VALUE
import kotlin.Double.Companion.MIN_VALUE
import kotlin.Double.Companion.NEGATIVE_INFINITY
import kotlin.Double.Companion.POSITIVE_INFINITY

internal class chupacabraKtTest {

    @Test
    fun logistic() {
        assertEquals(0.5, logistic(0.0), 1e-6)
        assertEquals(0.5, logistic(3.0, midPoint = 3.0), 1e-6)
        assertEquals(1.0, logistic(MAX_VALUE), 1e-6)
        assertEquals(0.5, logistic(MIN_VALUE), 1e-6)
        assertEquals(4.5, logistic(MAX_VALUE, maxValue = 4.5), 1e-6)
        assertEquals(0.9999938558253978, logistic(12.0), 1e-6)
        assertEquals(1.0 - 0.9999938558253978, logistic(-12.0), 1e-6)
        assertEquals(0.9975273768433652, logistic(12.0, growthRate = 0.5), 1e-6)
        assertEquals(1.0 - 0.9975273768433652, logistic(-12.0, growthRate = 0.5), 1e-6)
        assertEquals(1.0, logistic(POSITIVE_INFINITY), 1e-6)
        assertEquals(0.0, logistic(NEGATIVE_INFINITY), 1e-6)
    }

    @Test
    fun logit() {
        assertEquals(0.0, logit(0.5), 1e-6)
        assertEquals(12.0, logit(0.9999938558253978), 1e-6)
        assertEquals(0.6, logistic(logit(0.6)), 1e-6)
        assertEquals(POSITIVE_INFINITY, logit(1.0), 1e-6)
        assertEquals(NEGATIVE_INFINITY, logit(0.0), 1e-6)
    }

}