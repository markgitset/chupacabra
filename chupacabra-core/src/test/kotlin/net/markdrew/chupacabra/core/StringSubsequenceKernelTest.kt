@file:Suppress("SpellCheckingInspection")

package net.markdrew.chupacabra.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.system.measureNanoTime


class StringSubsequenceKernelTest {

    @Test
    fun testLambdaTooSmall() {
        assertThrows<IllegalArgumentException> { StringSubsequenceKernel(0.0) }
    }

    @Test
    fun testLambdaTooBig() {
        assertThrows<IllegalArgumentException> { StringSubsequenceKernel(1.000001) }
    }

    /**
     * Tests the non-normalized kernel value of two distinct strings.
     */
    @Test fun testRawKernel() {
        doRawTest(0.0, 2, 0.5, "abc", "xyzt") // should be zero
        doRawTest(0.25, 2, 0.5, "cat", "car")
        doRawTest(0.25, 2, 0.5, "car", "cat")
    }

    /**
     * Tests the non-normalized kernel value of two distinct strings.
     */
    @Suppress("JoinDeclarationAndAssignment")
    @Test fun testRawSelfKernel() {
        var lambda = 0.5
        var n: Int
        var s: String

        n = 1
        s = "a"
        doRawTest(1.0, n, lambda, s, s)
        s = "ab"
        doRawTest(2.0, n, lambda, s, s)
        s = "abc"
        doRawTest(3.0, n, lambda, s, s)
        s = "abcd"
        doRawTest(4.0, n, lambda, s, s)
        s = "abab"
        doRawTest(8.0, n, lambda, s, s)
        s = "aaaa"
        doRawTest(16.0, n, lambda, s, s)

        n = 2
        s = "a"
        doRawTest(0.0, n, lambda, s, s)
        s = "ab"
        doRawTest(0.25, n, lambda, s, s)
        s = "abc"
        doRawTest(0.5625, n, lambda, s, s)
        s = "abcd"
        doRawTest(0.890625, n, lambda, s, s)
        s = "abab"
        doRawTest(1.640625, n, lambda, s, s)
        s = "aaaa"
        doRawTest(4.515625, n, lambda, s, s)

        n = 3
        s = "a"
        doRawTest(0.0, n, lambda, s, s)
        s = "ab"
        doRawTest(0.0, n, lambda, s, s)
        s = "abc"
        doRawTest(0.0625, n, lambda, s, s)
        s = "abcd"
        doRawTest(0.15625, n, lambda, s, s)
        s = "abab"
        doRawTest(0.15625, n, lambda, s, s)
        s = "aaaa"
        doRawTest(0.5625, n, lambda, s, s)

        lambda = 0.25
        n = 2
        s = "a"
        doRawTest(0.0, n, lambda, s, s)
        s = "ab"
        doRawTest(0.0625, n, lambda, s, s)
        s = "abc"
        doRawTest(0.12890625, n, lambda, s, s)
        s = "abcd"
        doRawTest(0.195556640625, n, lambda, s, s)
        s = "abab"
        doRawTest(0.336181640625, n, lambda, s, s)
        s = "aaaa"
        doRawTest(0.793212890625, n, lambda, s, s)
    }

    /**
     * Tests that the non-normalized kernel value of two strings, where one has
     * length less than n, is zero.
     */
    @Test fun testRawKernelWithLengthLessThanN() {
        doRawTest(0.0, 4, 0.5, "abc", "abcd")
    }

    /**
     * Tests that the normalized kernel value of two strings, where one has
     * length less than n, is zero.
     */
    @Test fun testNormalizedKernelWithLengthLessThanN() {
        val kernel = StringSubsequenceKernel()
        assertEquals(0.0, kernel.normalizedKernel(4, "abc", "abcd"), EPSILON)
    }

    /**
     * Test that the kernel is commutative
     */
    @Test fun testCommutative() {
        val kernel = StringSubsequenceKernel()
        assertEquals(
            kernel.normalizedKernel(2, "commutative", "contemplate"),
            kernel.normalizedKernel(2, "contemplate", "commutative"),
            EPSILON
        )
    }

    /**
     * Based on example from "Text Classification using String Kernels" by Huma
     * Lodhi, et al. (2002) which shows that K(car,cat)=1/(2 + lambda^2)
     */
    @Test fun testCarAndCat() {
        val s1 = "car"
        val s2 = "cat"
        doTest(2.0 / 3.00, 1, 1.00, s1, s2)
        doTest(2.0 / 3.00, 1, 0.50, s1, s2)
        doTest(2.0 / 3.00, 1, 0.05, s1, s2)
        doTest(1.0 / 3.00, 2, 1.00, s1, s2)
        doTest(1.0 / 2.25, 2, 0.50, s1, s2)
        doTest(1.0 / 2.00, 2, 0.05, s1, s2)
    }

    /**
     * Shows that for n>1, decreasing lambda makes the kernel more sensitive to non-contiguous subsequences
     */
    @Test fun testDuneAndDestination() {
        val s1 = "dune"
        val s2 = "destination"
        doTest(0.485, 1, 1.00, s1, s2)
        doTest(0.485, 1, 0.50, s1, s2)
        doTest(0.485, 1, 0.05, s1, s2)
        doTest(0.126, 2, 1.00, s1, s2)
        doTest(0.036, 2, 0.50, s1, s2)
        doTest(0.000, 2, 0.05, s1, s2)
    }

    /**
     * Based on example from "Text Classification using String Kernels" by Huma
     * Lodhi, et al. (2002) which claims that K1=0.580 (I believe this one is
     * wrong--should be 0.849), K2=0.580, K3=0.478, K4=0.439, K5=0.406, and
     * K6=0.370. Also, the paper doesn't state what lambda value was used for
     * this example, so I inferred lambda=0.5 which yields matching results.
     */
    @Test fun testBlankIsOrganizedBlankExample() {
        val s1 = "science is organized knowledge"
        val s2 = "wisdom is organized life"
        val kernel = StringSubsequenceKernel(0.5)
        val expected = doubleArrayOf(0.849, 0.580, 0.478, 0.439, 0.406, 0.370)
        val n = expected.size
        for (i in 0 until n) {
            val ssk = kernel.normalizedKernel(i + 1, s1, s2)
            println("k=${i + 1}, lambda=0.5, SSK($s1,$s2)=$ssk")
            assertEquals(expected[i], ssk, EPSILON)
        }
    }

    @Disabled @Test
    fun testScalesLinearlyWithN() {
        // Currently, *seems* to scale as n^4?!
        val s1 = "science is organized knowledge"
        val s2 = "wisdom is organized life"
        val times = LongArray(8)
        time(1, 0.5, s1, s2)
        for (i in times.indices) {
            times[i] = time(i + 1, 0.5, s1, s2)
            println("time(n=" + (i + 1) + ") = " + times[i])
        }

        println()
        val diff1 = LongArray(times.size - 1)
        for (i in diff1.indices) {
            diff1[i] = times[i + 1] - times[i]
            println("diff1 = " + diff1[i])
        }

        println()
        val diff2 = LongArray(diff1.size - 1)
        for (i in diff2.indices) {
            diff2[i] = diff1[i + 1] - diff1[i]
            println("diff2 = " + diff2[i])
        }

        println()
        val diff3 = LongArray(diff2.size - 1)
        for (i in diff3.indices) {
            diff3[i] = diff2[i + 1] - diff2[i]
            println("diff3 = " + diff3[i])
        }

        println()
        val diff4 = LongArray(diff3.size - 1)
        for (i in diff4.indices) {
            diff4[i] = diff3[i + 1] - diff3[i]
            println("diff4 = " + diff4[i])
        }
    }

    @Disabled @Test
    fun testScalesLinearlyWithLength() {
        // Currently, *seems* sub-linear
        val s1 = "science is organized knowledge"
        val s2 = "wisdom is organized life"

        val ssk = StringSubsequenceKernel(0.5)
        for (n in 1..10) {
            val s1n = s1.repeat(n)
            val s2n = s2.repeat(n)
            val time = measureNanoTime {
                repeat(10) {
                    ssk.normalizedKernel(2, s1n, s2n)
                }
            }
            println("time(n=$n) = $time")
        }
    }


    @Test
    @Suppress("SpellCheckingInspection")
    fun testSsk() {
        val delta = 0.0000000000000001
        val ssk = StringSubsequenceKernel()
        assertEquals(0.0, ssk.normalizedKernel(2, "dog", ""), delta)
        assertEquals(0.0, ssk.normalizedKernel(2, "", "dog"), delta)
        assertEquals(1.0, ssk.normalizedKernel(2, "dog", "dog"), delta)
        assertEquals(0.4095595343695288, ssk.normalizedKernel(2, "kitten", "sitting"), delta)
        assertEquals(0.4095595343695288, ssk.normalizedKernel(2, "sitting", "kitten"), delta)
        assertEquals(0.23102730838352442, ssk.normalizedKernel(2, "rosettacode", "raisethysword"), delta)
        assertEquals(0.23102730838352442, ssk.normalizedKernel(2, "raisethysword", "rosettacode"), delta)
        assertEquals(0.3591613249289554, ssk.normalizedKernel(2, "sleep", "fleeting"), delta)
        assertEquals(0.3591613249289554, ssk.normalizedKernel(2, "fleeting", "sleep"), delta)
        assertEquals(0.631578947368421, ssk.normalizedKernel(2, "lawn", "flaw"), delta)
        assertEquals(0.631578947368421, ssk.normalizedKernel(2, "flaw", "lawn"), delta)
        assertEquals(0.0, ssk.normalizedKernel(2, "flaw", "ox"), delta)
        
//        // speed check
//        val time = measureTimeMillis { repeat(1_000_000) {
//            ssk.normalizedKernel(2, "rosettacode", "raisethysword")
//            ssk.normalizedKernel(2, "sleep", "fleeting")
//        } }
//        println("time = $time ms")
    }

    companion object {

        /** Test results tolerance */
        private const val EPSILON = 0.001

        private fun doTest(expectedSsk: Double, k: Int, lambda: Double, s1: String, s2: String) {
            val kernel = StringSubsequenceKernel(lambda)
            val ssk = kernel.normalizedKernel(k, s1, s2)
            println("k=$k, lambda=$lambda, normalSSK($s1,$s2)=$ssk")
            assertEquals(expectedSsk, ssk, EPSILON)
        }

        private fun doRawTest(expectedSsk: Double, k: Int, lambda: Double, s1: String, s2: String) {
            val kernel = StringSubsequenceKernel(lambda)
            val ssk = kernel.rawKernel(k, s1, s2)
            println("k=$k, lambda=$lambda, rawSSK($s1,$s2)=$ssk")
            assertEquals(expectedSsk, ssk, EPSILON)
        }

        private fun time(n: Int, lambda: Double, s1: String, s2: String): Long = measureNanoTime { 
            StringSubsequenceKernel(lambda).normalizedKernel(n, s1, s2)
        }

    }

}
