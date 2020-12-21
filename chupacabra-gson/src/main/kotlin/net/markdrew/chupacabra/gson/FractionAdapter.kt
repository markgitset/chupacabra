package net.markdrew.chupacabra.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.markdrew.chupacabra.core.Fraction
import java.lang.reflect.Type

/**
 * Serializes and deserializes [Fraction]s as JSON strings.  For example, given a delimiter of "/", this adapter will convert the 
 * fraction 2/3 to and from a JSON primitive string value of "2/3".
 *
 * @param delimiter string that separates the numerator and denominator (defaults to "/")
 */
class FractionAdapter(private val delimiter: String = "/") : JsonSerializer<Fraction>, JsonDeserializer<Fraction> {

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Fraction {
        val s: String = json.asString
        val i: Int = s.indexOf(delimiter)
        if (i < 0) {
            throw JsonParseException("Unable to parse '$s' as Fraction.  (It should look something like '1${delimiter}3'.)")
        }
        val numerator = s.take(i).toInt()
        val denominator = s.drop(i + delimiter.length).toInt()
        return Fraction(numerator, denominator)
    }

    override fun serialize(fraction: Fraction, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
        JsonPrimitive("${fraction.numerator}$delimiter${fraction.denominator}")

}
