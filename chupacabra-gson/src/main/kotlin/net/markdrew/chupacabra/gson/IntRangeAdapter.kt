package net.markdrew.chupacabra.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import java.lang.reflect.Type

/**
 * Serializes [IntRange]s as the raw feature string.  For example, given a delimiter of "_", this adapter will convert the int
 * range 1..3 to and from a JSON primitive string value of "1_3".
 *
 * @param delimiter string that separates the start and end indices (defaults to "..")
 */
class IntRangeAdapter(
    private val delimiter: String = "..", 
    private val inclusiveEndPoints: Boolean = false
) : JsonSerializer<IntRange>, JsonDeserializer<IntRange> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): IntRange {
        val s: String = json.asString
        val i: Int = s.indexOf(delimiter)
        if (i < 0) {
            throw JsonParseException("Unable to parse '$s' as an IntRange.  (It should look something like '1${delimiter}3'.)")
        }
        val start = s.take(i).toInt()
        val end = s.drop(i + delimiter.length).toInt()
        return IntRange(start, if (inclusiveEndPoints) end else end - 1)
    }

    override fun serialize(range: IntRange, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val end: Int = if (inclusiveEndPoints) range.last else range.last + 1
        return JsonPrimitive("${range.first}$delimiter$end")
    }

}
