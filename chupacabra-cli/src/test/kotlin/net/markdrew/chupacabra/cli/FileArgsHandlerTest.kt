package net.markdrew.chupacabra.cli

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class FileArgsHandlerTest {
    
    @Test
    fun testAmendSuffix() {
        assertEquals("test", FileArgsHandler.amendSuffix("test", "", ""))
        assertEquals("tes", FileArgsHandler.amendSuffix("test", "", "t"))
        assertEquals("te", FileArgsHandler.amendSuffix("test", "", "st"))
        assertEquals("text", FileArgsHandler.amendSuffix("test", "xt", "st"))
        assertEquals("tester", FileArgsHandler.amendSuffix("test", "er", ""))
        assertEquals("test", FileArgsHandler.amendSuffix("test", "", "es"))
    }

}