package net.markdrew.chupacabra.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type
import java.nio.ByteBuffer
import java.util.Base64

/**
 * Serializes [ByteBuffer]s as base64-encoded strings.
 */
object ByteBufferAdapter : JsonDeserializer<ByteBuffer>, JsonSerializer<ByteBuffer> {

    override fun serialize(src: ByteBuffer, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
            JsonPrimitive(Base64.getEncoder().encodeToString(src.array()))

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): ByteBuffer =
            ByteBuffer.wrap(Base64.getDecoder().decode(json.asString))

}