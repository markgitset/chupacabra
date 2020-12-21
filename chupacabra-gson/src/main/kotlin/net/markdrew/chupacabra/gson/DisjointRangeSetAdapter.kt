package net.markdrew.chupacabra.gson

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import net.markdrew.chupacabra.core.DisjointRangeSet
import java.lang.reflect.Type

/**
 * GSON deserializer for [DisjointRangeSet]s.
 *
 * Register this adapter (and [IntRangeAdapter]) when you are creating your GSON instance like this:
 *
 * <pre>
 * Gson gson = new GsonBuilder().registerTypeAdapter(IntRange::class.java, IntRangeAdapter("_"))
 *                              .registerTypeAdapter(DisjointRangeSet::class.java, DisjointRangeSetAdapter()).create();
 * </pre>
 *
 * Given a [DisjointRangeSet] containing 1..3 and 4..10, gson will read/write JSON like this:
 *
 * <pre>
 * [
 *   "1_3",
 *   "4_10"
 * ]
 * </pre>
 *
 * This format will serialize and deserialize just fine as long as this adapter is registered.
 */
class DisjointRangeSetAdapter : JsonDeserializer<DisjointRangeSet> {

    /**
     * Gson invokes this call-back method during deserialization when it encounters a [DisjointRangeSet] field.
     *
     * @param json The Json data being deserialized
     * @param typeOfT The type of the Object to deserialize to
     * @return a deserialized [DisjointRangeSet]
     */
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext): DisjointRangeSet =
            DisjointRangeSet(context.deserialize<Collection<IntRange>>(json, typeOf<Collection<IntRange>>()))

}
