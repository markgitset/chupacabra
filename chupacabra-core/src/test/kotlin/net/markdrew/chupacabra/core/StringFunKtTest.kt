package net.markdrew.chupacabra.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class StringFunKtTest {

    @Test
    fun testRegularPlural() {
        assertEquals("0 dogs", plural(0, "dog"))
        assertEquals("1 dog", plural(1, "dog"))
        assertEquals("2 dogs", plural(2, "dog"))
    }

    @Test
    fun testIrregularPlural() {
        assertEquals("0 mice", plural(0, "mouse", "mice"))
        assertEquals("1 mouse", plural(1, "mouse", "mice"))
        assertEquals("2 mice", plural(2, "mouse", "mice"))
    }

    @Test
    @Suppress("SpellCheckingInspection")
    fun testLevenshtein() {
        assertEquals(3, levenshtein("dog", ""))
        assertEquals(3, levenshtein("", "dog"))
        assertEquals(0, levenshtein("dog", "dog"))
        assertEquals(3, levenshtein("kitten", "sitting"))
        assertEquals(3, levenshtein("sitting", "kitten"))
        assertEquals(8, levenshtein("rosettacode", "raisethysword"))
        assertEquals(8, levenshtein("raisethysword", "rosettacode"))
        assertEquals(5, levenshtein("sleep", "fleeting"))
        assertEquals(5, levenshtein("fleeting", "sleep"))
        assertEquals(2, levenshtein("lawn", "flaw"))
        assertEquals(2, levenshtein("flaw", "lawn"))
        assertEquals(4, levenshtein("flaw", "ox"))
//        val time = measureTimeMillis { repeat(10_000_000) {
//            levenshtein("rosettacode", "raisethysword")
//            levenshtein("sleep", "fleeting")
//        } }
//        println("time = $time ms")
    }

    @Test
    fun testSubstringBeforeNth() {
        assertEquals("aa", "aaaaaa".substringBeforeNth("aa", 2))
        assertEquals("aa", "aaaaaa".substringBeforeNth("aa", -2))

        assertEquals("b", "banana".substringBeforeNth("a", 1))
        assertEquals("ban", "banana".substringBeforeNth("a", 2))
        assertEquals("banan", "banana".substringBeforeNth("a", 3))
        assertEquals("banana", "banana".substringBeforeNth("a", 4))
        assertEquals("banan", "banana".substringBeforeNth("a", -1))
        assertEquals("ban", "banana".substringBeforeNth("a", -2))
        assertEquals("b", "banana".substringBeforeNth("a", -3))
        assertEquals("", "banana".substringBeforeNth("a", -4))

        assertEquals("banana", "banana".substringBeforeNth("x", 1))
        assertEquals("", "banana".substringBeforeNth("x", -2))
    }

    @Test
    fun testSubstringAfterNth() {
        assertEquals("aa", "aaaaaa".substringAfterNth("aa", 2))
        assertEquals("aa", "aaaaaa".substringAfterNth("aa", -2))

        assertEquals("nana", "banana".substringAfterNth("a", 1))
        assertEquals("na", "banana".substringAfterNth("a", 2))
        assertEquals("", "banana".substringAfterNth("a", 3))
        assertEquals("", "banana".substringAfterNth("a", -1))
        assertEquals("na", "banana".substringAfterNth("a", -2))
        assertEquals("nana", "banana".substringAfterNth("a", -3))
        assertEquals("banana", "banana".substringAfterNth("a", -4))

        assertEquals("", "banana".substringAfterNth("x", 1))
        assertEquals("banana", "banana".substringAfterNth("x", -2))
    }
}