package net.markdrew.chupacabra.core

import java.util.LinkedHashMap
import kotlin.collections.MutableMap.MutableEntry

/**
 * Simple extension of [LinkedHashMap] that implements a least-recently-used (LRU) cache.
 *
 * @param <K> type of the keys
 * @param <V> type of the values
 */
class CacheMap<K, V>(var cacheSize: Int) : LinkedHashMap<K, V>(cacheSize, 0.75f, true) {

    override fun removeEldestEntry(eldest: MutableEntry<K, V>?): Boolean = size > cacheSize

}