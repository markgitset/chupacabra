package net.markdrew.chupacabra.core

/**
 * Schedule some action to run after the sequence is exhausted (e.g., close a file)
 * NOTE, however, that it's not really safe to rely on this to close a file since an exception during iteration
 * will NOT result in [action] being called.
 */
fun <T> Sequence<T>.doAfterLast(action: () -> Unit): Sequence<T> = sequence {
    yieldAll(iterator())
    action()
}

/**
 * Returns a sequence in which runs of equal elements are squashed into a single element.  For example with [Int]s,
 * { 1, 2, 2, 3, 2, 2, 2, 4, 4, 5 } would become { 1, 2, 3, 2, 4, 5 }.
 *
 * The elements in the resulting sequence are in the same order as their runs in the source sequence.
 *
 * The operation is _intermediate_ and _stateless_.
 */
fun <T> Sequence<T>.squashRuns(): Sequence<T> = squashRunsBy { it }

/**
 * Returns a sequence in which each run of elements with equal keys (as determined by the given [selector] function)
 * is replaced by the first element of the run.  For example with [Int]s, { 1, 2, 2, 3, 2, 2, 2, 4, 4, 5 } would become
 * { 1, 2, 3, 2, 4, 5 }. As a more detailed example, these two expressions are equivalent:
 *
 *     sequenceOf(11, 12, 23, 14, 15, 26, 27, 48, 49, 40, 51, 62, 93, 94, 25).squashRunsBy { it / 10 }
 *     sequenceOf(11, 23, 14, 26, 48, 51, 62, 93, 25)
 *
 * The elements in the resulting sequence are in the same order as their runs in the source sequence.
 *
 * The operation is _intermediate_ and _stateless_.
 */
fun <T, K> Sequence<T>.squashRunsBy(selector: (T) -> K): Sequence<T> = sequence {
    val iterator: Iterator<T> = iterator()
    if (iterator.hasNext()) {
        var currentVal: T = iterator.next()
        yield(currentVal)
        var prevKey: K = selector(currentVal)
        while (iterator.hasNext()) {
            currentVal = iterator.next()
            val currentKey = selector(currentVal)
            if (currentKey != prevKey) {
                yield(currentVal)
                prevKey = currentKey
            }
        }
    }
}
