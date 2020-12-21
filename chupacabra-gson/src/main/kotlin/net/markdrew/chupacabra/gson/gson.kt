package net.markdrew.chupacabra.gson

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * Convenience method to get the runtime type of a parameterized (or unparameterized) type
 */
inline fun <reified T> typeOf(): Type {
    val javaClass: Class<T> = T::class.java
    return if (javaClass.typeParameters.isEmpty()) javaClass else object : TypeToken<T>() {}.type
}
