package net.markdrew.chupacabra.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import net.markdrew.chupacabra.core.TestEnum.MessY
import net.markdrew.chupacabra.core.TestEnum.SIMPLE
import net.markdrew.chupacabra.core.TestEnum.W_UNDER

private enum class TestEnum { SIMPLE, MessY, W_UNDER }

internal class EnumFunKtTest {
    
    @Test
    fun testLowercase() {
        assertEquals("simple", SIMPLE.toLowerCase())
        assertEquals("messy", MessY.toLowerCase())
        assertEquals("w_under", W_UNDER.toLowerCase())
    }
    
    @Test
    fun testParseEnum() {
        assertThrows<IllegalArgumentException> { parseEnum<TestEnum>("nope") }
        assertThrows<IllegalArgumentException> { parseEnum<TestEnum>("") }
        
        assertThrows<IllegalArgumentException> {
            parseEnum<TestEnum>(
                    "simple", caseSensitive = true
            )
        }
        assertEquals(SIMPLE, parseEnum<TestEnum>("simple"))
        assertEquals(SIMPLE, parseEnum<TestEnum>("SIMPLE", caseSensitive = true))
        
        assertThrows<IllegalArgumentException> {
            parseEnum<TestEnum>(
                    "messy", caseSensitive = true
            )
        }
        assertEquals(MessY, parseEnum<TestEnum>("mESSy"))
        assertEquals(MessY, parseEnum<TestEnum>("MessY", caseSensitive = true))
    }
    
    @Test
    fun testParseEnumOrNull() {
        assertNull(parseEnumOrNull<TestEnum>("nope"))
        assertNull(parseEnumOrNull<TestEnum>(""))
        
        assertNull(parseEnumOrNull<TestEnum>("simple", caseSensitive = true))
        assertEquals(SIMPLE, parseEnumOrNull<TestEnum>("simple"))
        assertEquals(SIMPLE, parseEnumOrNull<TestEnum>("SIMPLE", caseSensitive = true))
        
        assertNull(parseEnumOrNull<TestEnum>("messy", caseSensitive = true))
        assertEquals(MessY, parseEnumOrNull<TestEnum>("mESSy"))
        assertEquals(MessY, parseEnumOrNull<TestEnum>("MessY", caseSensitive = true))
    }

}