package net.markdrew.chupacabra.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class CacheMapTest {

    @Test
    fun `evicts eldest entry when capacity is exceeded`() {
        val cache = CacheMap<String, String>(3)
        cache["A"] = "1"
        cache["B"] = "2"
        cache["C"] = "3"

        assertEquals(3, cache.size)

        // Add a 4th element, "A" should be evicted
        cache["D"] = "4"

        assertEquals(3, cache.size)
        assertNull(cache["A"])
        assertEquals("2", cache["B"])
        assertEquals("3", cache["C"])
        assertEquals("4", cache["D"])
    }

    @Test
    fun `accessing entry prevents it from being evicted`() {
        val cache = CacheMap<String, String>(3)
        cache["A"] = "1"
        cache["B"] = "2"
        cache["C"] = "3"

        // Access "A", making "B" the eldest
        val accessed = cache["A"]
        assertEquals("1", accessed)

        // Add a 4th element, "B" should be evicted
        cache["D"] = "4"

        assertEquals(3, cache.size)
        assertNull(cache["B"])
        assertEquals("1", cache["A"])
        assertEquals("3", cache["C"])
        assertEquals("4", cache["D"])
    }

    @Test
    fun `dynamically updating cacheSize affects eviction`() {
        val cache = CacheMap<String, String>(5)
        cache["A"] = "1"
        cache["B"] = "2"

        // Reduce cache size to 2
        cache.cacheSize = 2

        // Add a 3rd element, "A" should be evicted because size (3) > cacheSize (2)
        cache["C"] = "3"

        assertEquals(2, cache.size)
        assertNull(cache["A"])
        assertEquals("2", cache["B"])
        assertEquals("3", cache["C"])
    }
}
