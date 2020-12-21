package net.markdrew.chupacabra.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class SequenceFunKtTest {

    @Test
    fun after() {
        var didIt = false
        listOf("a", "b", "c").asSequence().doAfterLast { didIt = true }.forEach { }
        assertTrue(didIt)
    }

    @Test
    fun squashRuns() {
        assertEquals(
            listOf(1, 2, 1, 2, 4, 5, 6, 9, 2),
            sequenceOf(1, 1, 2, 1, 1, 2, 2, 4, 4, 4, 5, 6, 9, 9, 2).squashRuns().toList()
        )
    }

    @Test
    fun squashRunsBy() {
        assertEquals(
            listOf(11, 23, 14, 26, 48, 51, 62, 93, 25),
            sequenceOf(11, 12, 23, 14, 15, 26, 27, 48, 49, 40, 51, 62, 93, 94, 25).squashRunsBy { it / 10 }.toList()
        )
    }

}