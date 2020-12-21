package net.markdrew.chupacabra.gson

import com.google.common.collect.ListMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.Multimaps
import com.google.common.collect.SetMultimap
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.ArrayList
import java.util.LinkedHashMap
import java.util.LinkedHashSet

/**
 * Adapts [Multimap]s to JSON arrays. Each map entry is an n-element array containing a key and a set/list of values. This
 * approach permits any type for the map's keys, and not just strings.
 *
 * Register this adapter when you are creating your GSON instance.
 *
 * <pre>
 * Gson gson = new GsonBuilder().registerTypeAdapter(Map.class, new MultimapTypeAdapterFactory()).create();
 * </pre>
 *
 * For a multimap with Point-valued keys and String-valued values, this might produce a two-dimensional JSON array like this,
 * where the outer array contains the map entries. The first value of each inner array is a Point, and the remaining values of
 * each inner array are the String values associated with the point.
 *
 * <pre>
 * [
 *   [
 *     {
 *       "x": 5,
 *       "y": 6
 *     },
 *     "a",
 *     "b",
 *   ],
 *   [
 *     {
 *       "x": 8,
 *       "y": 8
 *     },
 *     "b",
 *     "z",
 *     "r"
 *   ]
 * }
 *</pre>
 *
 * This format will serialize and deserialize just fine as long as this adapter is registered.
 *
 * Note that this factory assumes the adapted type is a [SetMultimap] unless the given typeToken is explicitly a
 * [ListMultimap]. Adapted from the example in the Javadoc of [TypeAdapterFactory].
 *
 * If you prefer to only deal with non-null multimaps, you can set suppressEmpty to true.  If, however, you need to preserve
 * the distinction between null and empty, do not suppress empty values.
 *
 * @param suppressEmpty if true, outputs no JSON for empty [Multimap]s and returns an empty [Multimap] when
 *            reading a null
 */
class MultimapTypeAdapterFactory(private val suppressEmpty: Boolean = false) : TypeAdapterFactory {

    companion object {
        private fun <K, V> newListMultimap(): ListMultimap<K, V> =
                Multimaps.newListMultimap(LinkedHashMap<K, Collection<V>>(), ::ArrayList)

        private fun <K, V> newSetMultimap(): SetMultimap<K, V> =
                Multimaps.newSetMultimap(LinkedHashMap<K, Collection<V>>(), ::LinkedHashSet)

        private fun <K, V> instantiator(listBased: Boolean): () -> Multimap<K, V> =
                if (listBased) this::newListMultimap else this::newSetMultimap
    }

    override fun <T> create(gson: Gson, typeToken: TypeToken<T>): TypeAdapter<T>? {
        val type = typeToken.type

        if (!Multimap::class.java.isAssignableFrom(typeToken.rawType)) {
            return null
        }

        if (type !is ParameterizedType) {
            throw RuntimeException("Not adapting raw type ($type).  " +
                                   "Make sure you're calling gson.toJson(obj, type) and not just gson.toJson(obj).")
        }

        val isListMultimap = ListMultimap::class.java.isAssignableFrom(typeToken.rawType)
        val (keyType: Type, valueType: Type) = type.actualTypeArguments
        val keyAdapter: TypeAdapter<out Any> = gson.getAdapter(TypeToken.get(keyType))
        val valueAdapter: TypeAdapter<out Any> = gson.getAdapter(TypeToken.get(valueType))

        @Suppress("UNCHECKED_CAST")
        return newMultimapAdapter(instantiator(isListMultimap), keyAdapter, valueAdapter, suppressEmpty) as TypeAdapter<T>?
    }

    private fun <K, V> newMultimapAdapter(instantiator: () -> Multimap<K, V>,
                                          keyAdapter: TypeAdapter<K>,
                                          valueAdapter: TypeAdapter<V>,
                                          suppressEmpty: Boolean): TypeAdapter<Multimap<K, V>> {

        return object : TypeAdapter<Multimap<K, V>>() {

            override fun write(out: JsonWriter, map: Multimap<K, V>?) {
                if (map == null || suppressEmpty && map.isEmpty) {
                    out.nullValue()
                    return
                }

                out.beginArray()
                for ((key, values) in map.asMap()) {
                    out.beginArray()
                    // key is first element of JSON array
                    keyAdapter.write(out, key)
                    // values are remaining elements of JSON array
                    values.forEach { valueAdapter.write(out, it) }
                    out.endArray()
                }
                out.endArray()
            }

            override fun read(input: JsonReader): Multimap<K, V>? {

                // if isNull, there's either a null multimap or an empty multimap (depending on suppressEmpty)
                val isNull = input.peek() == JsonToken.NULL

                if (isNull) {
                    input.nextNull()
                    // not suppressing empty multimaps, so return null
                    if (!suppressEmpty) return null
                }

                val result = instantiator()

                // since we're suppressing empty multimaps and we got a null, we must assume and return an empty multimap
                if (isNull) return result

                input.beginArray()
                while (input.hasNext()) {
                    input.beginArray()
                    // key is first element of JSON array
                    val key: K = keyAdapter.read(input)
                    // values are remaining elements of JSON array
                    val values = ArrayList<V>()
                    while (input.hasNext()) {
                        values.add(valueAdapter.read(input))
                    }
                    result.putAll(key, values)
                    input.endArray()
                }
                input.endArray()
                return result
            }
        }
    }
}
