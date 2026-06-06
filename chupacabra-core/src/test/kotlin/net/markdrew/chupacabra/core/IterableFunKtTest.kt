package net.markdrew.chupacabra.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class IterableFunKtTest {

    @Test
    fun squashRuns() {
        assertEquals(
            listOf(1, 2, 1, 2, 4, 5, 6, 9, 2),
            listOf(1, 1, 2, 1, 1, 2, 2, 4, 4, 4, 5, 6, 9, 9, 2).squashRuns()
        )
        assertEquals(
            emptyList<Int>(),
            emptyList<Int>().squashRuns()
        )
        assertEquals(
            listOf("a"),
            listOf("a", "a", "a").squashRuns()
        )
    }

    @Test
    fun squashRunsBy() {
        assertEquals(
            listOf(11, 23, 14, 26, 48, 51, 62, 93, 25),
            listOf(11, 12, 23, 14, 15, 26, 27, 48, 49, 40, 51, 62, 93, 94, 25).squashRunsBy { it / 10 }
        )
        assertEquals(
            emptyList<Int>(),
            emptyList<Int>().squashRunsBy { it / 10 }
        )
        assertEquals(
            listOf("ab"),
            listOf("ab", "ac", "ad").squashRunsBy { it[0] }
        )
    }

    @Test
    fun squashRunsByTo() {
        val dest = mutableListOf<Int>()
        assertEquals(
            listOf(11, 23, 14, 26, 48, 51, 62, 93, 25),
            listOf(11, 12, 23, 14, 15, 26, 27, 48, 49, 40, 51, 62, 93, 94, 25).squashRunsByTo(dest) { it / 10 }
        )
        assertEquals(listOf(11, 23, 14, 26, 48, 51, 62, 93, 25), dest)
    }

}
