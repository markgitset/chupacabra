package net.markdrew.chupacabra.gson

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.reflect.ParameterizedType

class GsonUtilsTest {

    @Test fun testTypeOfSimple() {
        assertEquals(String::class.java, typeOf<String>())
    }

    @Test fun testTypeOfGeneric() {
        val type: ParameterizedType = typeOf<List<String>>() as ParameterizedType
        assertEquals(List::class.java, type.rawType)
        assertEquals(1, type.actualTypeArguments.size)
        assertEquals("? extends java.lang.String", type.actualTypeArguments[0].typeName)
    }

}
