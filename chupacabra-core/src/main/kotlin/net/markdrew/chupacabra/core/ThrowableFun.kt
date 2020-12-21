package net.markdrew.chupacabra.core

/**
 * Given a [Collection] of some [Throwable] types, returns the first [Throwable] after attaching (and suppressing) any
 * remaining [Throwable]s.  Returns null if the [Collection] is empty.
 */
fun <T : Throwable> Collection<T>.suppressSubsequent(): T? = if (isEmpty()) null else {
    drop(1).foldRight(first()) { s, acc -> acc.apply { addSuppressed(s) } }
}

/**
 * Throws the first [Throwable] in the [Collection] after attaching (and suppressing) any
 * remaining [Throwable]s.  Does nothing if the [Collection] is empty.
 */
fun <T : Throwable> Collection<T>.throwFirst() = suppressSubsequent()?.let { throw it }

/**
 * Calls the given [fn] returning null if no [Throwable]s were thrown.  If a [Throwable] was thrown,
 * it is caught and returned.
 */
inline fun nullOrThrowable(fn: () -> Unit): Throwable? = try { fn(); null } catch (t: Throwable) { t }