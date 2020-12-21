package net.markdrew.chupacabra.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File
import java.util.Properties

class PropertiesFunKtTest {

    private val propMap = linkedMapOf(
            "this" to "that",
            "big" to "false",
            "small" to "true",
            "count" to "5"
    )
    
    private val properties = Properties().apply {
        setProperty("this", "that")
        setProperty("big", "false")
        setProperty("small", "true")
        setProperty("count", "5")
    }

    @Test
    fun toStringMap() {
        assertEquals(propMap, properties.toStringMap())
    }

    @Test
    fun toProperties() {
        assertEquals(properties, propMap.toProperties())
    }

    @Test
    fun readWriteAsProperties() {
        val tmpFile: File = createTempFile().apply { deleteOnExit() }
        propMap.writeAsProperties(tmpFile)
        assertEquals(propMap, readPropertiesAsMap(tmpFile))
    }

    @Test
    fun readWrite() {
        val tmpFile: File = createTempFile().apply { deleteOnExit() }
        properties.write(tmpFile)
        assertEquals(properties, readProperties(tmpFile))
    }

    @Test
    fun getBooleanFromProperties() {
        assertFalse(properties.getBooleanProperty("missing"))
        assertTrue(properties.getBooleanProperty("also-missing", true))
        assertFalse(properties.getBooleanProperty("big", true))
        assertTrue(properties.getBooleanProperty("small"))
        assertFalse(properties.getBooleanProperty("count"))
    }

    @Test
    fun getBooleanFromStringMap() {
        assertFalse(propMap.getBooleanProperty("missing"))
        assertTrue(propMap.getBooleanProperty("also-missing", true))
        assertFalse(propMap.getBooleanProperty("big", true))
        assertTrue(propMap.getBooleanProperty("small"))
        assertFalse(propMap.getBooleanProperty("count"))
    }
}