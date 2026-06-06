package net.markdrew.chupacabra.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class CompareFunTest {

    @Test
    fun testWithLastValue() {
        val comparator = naturalOrder<Int>().withLastValue(5)

        val list = listOf(5, 3, 1, 5, 2, 4)
        val sorted = list.sortedWith(comparator)

        assertEquals(listOf(1, 2, 3, 4, 5, 5), sorted)
    }

    @Test
    fun testWithLastValue_multiple() {
        val comparator = naturalOrder<Int>().withLastValue(2)

        val list = listOf(1, 2, 3, 2, 4, 2)
        val sorted = list.sortedWith(comparator)

        assertEquals(listOf(1, 3, 4, 2, 2, 2), sorted)
    }

    @Test
    fun testWithLastValue_onlyLastValues() {
        val comparator = naturalOrder<Int>().withLastValue(7)

        val list = listOf(7, 7, 7)
        val sorted = list.sortedWith(comparator)

        assertEquals(listOf(7, 7, 7), sorted)
    }

    @Test
    fun testWithLastValue_noLastValues() {
        val comparator = naturalOrder<Int>().withLastValue(9)

        val list = listOf(4, 1, 3, 2)
        val sorted = list.sortedWith(comparator)

        assertEquals(listOf(1, 2, 3, 4), sorted)
    }

    @Test
    fun testWithLastValue_reverseOrder() {
        val comparator = reverseOrder<Int>().withLastValue(1)

        val list = listOf(1, 3, 2, 1, 4)
        val sorted = list.sortedWith(comparator)

        assertEquals(listOf(4, 3, 2, 1, 1), sorted)
    }

    @Test
    fun testWithLastValue_string() {
        val comparator = naturalOrder<String>().withLastValue("apple")

        val list = listOf("banana", "apple", "cherry", "apple", "date")
        val sorted = list.sortedWith(comparator)

        assertEquals(listOf("banana", "cherry", "date", "apple", "apple"), sorted)
    }
}
