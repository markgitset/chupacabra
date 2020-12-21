package net.markdrew.chupacabra.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.io.File
import java.lang.reflect.Type

/**
 * Adapts [File]s for use with GSON.
 */
object FileAdapter : JsonSerializer<File>, JsonDeserializer<File> {

    override fun serialize(src: File?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement =
            JsonPrimitive(src.toString())

    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): File =
            File(json?.asString)

}
