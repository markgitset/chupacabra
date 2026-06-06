package net.markdrew.chupacabra.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

internal class ThrowableFunKtTest {

    @Test
    fun testSuppressSubsequentEmpty() {
        val throwables = emptyList<Throwable>()
        assertNull(throwables.suppressSubsequent())
    }

    @Test
    fun testSuppressSubsequentSingle() {
        val t = RuntimeException("1")
        val throwables = listOf(t)
        val result = throwables.suppressSubsequent()
        assertSame(t, result)
        assertEquals(0, result?.suppressed?.size)
    }

    @Test
    fun testSuppressSubsequentMultiple() {
        val t1 = RuntimeException("1")
        val t2 = RuntimeException("2")
        val t3 = RuntimeException("3")
        val throwables = listOf(t1, t2, t3)
        val result = throwables.suppressSubsequent()

        assertSame(t1, result)
        assertEquals(2, result?.suppressed?.size)
        // Check order based on current implementation
        val suppressed = result?.suppressed
        assertSame(t3, suppressed?.get(0))
        assertSame(t2, suppressed?.get(1))
    }

    @Test
    fun testThrowFirstEmpty() {
        val throwables = emptyList<Throwable>()
        // Should not throw
        throwables.throwFirst()
    }

    @Test
    fun testThrowFirstSingle() {
        val t = RuntimeException("1")
        val throwables = listOf(t)
        val thrown = assertThrows(RuntimeException::class.java) {
            throwables.throwFirst()
        }
        assertSame(t, thrown)
        assertEquals(0, thrown.suppressed.size)
    }

    @Test
    fun testThrowFirstMultiple() {
        val t1 = RuntimeException("1")
        val t2 = RuntimeException("2")
        val t3 = RuntimeException("3")
        val throwables = listOf(t1, t2, t3)
        val thrown = assertThrows(RuntimeException::class.java) {
            throwables.throwFirst()
        }

        assertSame(t1, thrown)
        assertEquals(2, thrown.suppressed.size)
        val suppressed = thrown.suppressed
        assertSame(t3, suppressed[0])
        assertSame(t2, suppressed[1])
    }

    @Test
    fun testNullOrThrowableSuccess() {
        val result = nullOrThrowable {
            // Do nothing, success
            val a = 1 + 1
            assertEquals(2, a)
        }
        assertNull(result)
    }

    @Test
    fun testNullOrThrowableFailure() {
        val t = RuntimeException("failure")
        val result = nullOrThrowable {
            throw t
        }
        assertSame(t, result)
    }
}
