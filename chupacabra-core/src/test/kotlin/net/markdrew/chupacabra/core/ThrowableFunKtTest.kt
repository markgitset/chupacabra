package net.markdrew.chupacabra.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class ThrowableFunKtTest {

    @Test
    fun testNullOrThrowableReturnsNullWhenNoExceptionThrown() {
        val result = nullOrThrowable {
            // Do nothing, no exception thrown
            val x = 1 + 1
        }
        assertNull(result)
    }

    @Test
    fun testNullOrThrowableReturnsThrowableWhenExceptionThrown() {
        val expectedException = RuntimeException("Test exception")
        val result = nullOrThrowable {
            throw expectedException
        }
        assertEquals(expectedException, result)
    }
}
