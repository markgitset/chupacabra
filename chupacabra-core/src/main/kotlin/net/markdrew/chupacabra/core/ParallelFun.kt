package net.markdrew.chupacabra.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking

/**
 * Runs the given function, [f], on each element of this collection in parallel.  After all elements have been processed, returns
 * the mapped collection.
 * 
 * @param postMapFun an optional function to call after each item has been mapped (e.g., for updating a progress bar)
 */
fun <T, U> Iterable<T>.parallelMap(postMapFun: () -> Unit = {}, f: suspend (T) -> U): List<U> = runBlocking(Dispatchers.Default) {
    map { async { f(it) } }.map { it.await().also { postMapFun() } }
}
