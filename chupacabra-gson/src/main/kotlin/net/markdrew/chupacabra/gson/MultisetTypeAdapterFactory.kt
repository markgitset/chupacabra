package net.markdrew.chupacabra.gson

import com.google.common.collect.LinkedHashMultiset
import com.google.common.collect.Multiset
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.lang.reflect.ParameterizedType

/**
 * Adapts [Multiset]s to JSON arrays, where the number of occurrences precedes each value in the set.
 *
 * Register this adapter when you are creating your GSON instance.
 *
 * <pre>
 * Gson gson = new GsonBuilder().registerTypeAdapter(Map.class, new MultisetTypeAdapterFactory()).create();
 * </pre>
 *
 * Given a String-valued multiset containing one "a" and three "b"s, this would emit JSON like this:
 *
 * <pre>
 * [
 *   1,
 *   "a",
 *   3,
 *   "b"
 * ]
 * </pre>
 *
 * This format will serialize and deserialize just fine as long as this adapter is registered.
 *
 * Adapted from the example in the Javadoc of [TypeAdapterFactory].
 * 
 * If you prefer to only deal with non-null multisets, you can set suppressEmpty to true.  If, however, you need to preserve
 * the distinction between null and empty, do not suppress empty values.
 *
 * @param suppressEmpty if true, outputs no JSON for empty [Multiset]s and returns an empty [Multiset] when
 *            reading a null
 */
class MultisetTypeAdapterFactory(private val suppressEmpty: Boolean = false) : TypeAdapterFactory {

    override fun <T> create(gson: Gson, typeToken: TypeToken<T>): TypeAdapter<T>? {
        val type = typeToken.type
        if (!Multiset::class.java.isAssignableFrom(typeToken.rawType) || type !is ParameterizedType) {
            return null
        }

        val elementType = type.actualTypeArguments[0]
        val elementAdapter = gson.getAdapter(TypeToken.get(elementType))

        @Suppress("UNCHECKED_CAST")
        return newMultisetAdapter(elementAdapter, suppressEmpty) as TypeAdapter<T>?
    }

    private fun <E> newMultisetAdapter(elementAdapter: TypeAdapter<E>,
                                       suppressEmpty: Boolean): TypeAdapter<Multiset<E>> {
        return object : TypeAdapter<Multiset<E>>() {

            override fun write(out: JsonWriter, value: Multiset<E>?) {
                if (value == null || suppressEmpty && value.isEmpty()) {
                    out.nullValue()
                    return
                }

                out.beginArray()
                for (entry in value.entrySet()) {
                    out.value(entry.count.toLong())
                    elementAdapter.write(out, entry.element)
                }
                out.endArray()
            }

            override fun read(input: JsonReader): Multiset<E>? {

                // if isNull, there's either a null multiset or an empty multiset (depending on suppressEmpty)
                val isNull = input.peek() == JsonToken.NULL

                if (isNull) {
                    input.nextNull()
                    // not suppressing empty multisets, so return null
                    if (!suppressEmpty) return null
                }

                val result = LinkedHashMultiset.create<E>()

                // since we're suppressing empty multisets and we got a null, we must assume and return an empty multiset
                if (isNull) return result

                input.beginArray()
                while (input.hasNext()) {
                    val count = input.nextInt()
                    val element = elementAdapter.read(input)
                    result.add(element, count)
                }
                input.endArray()
                return result
            }
        }
    }
}
