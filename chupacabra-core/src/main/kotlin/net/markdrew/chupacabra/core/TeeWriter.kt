package net.markdrew.chupacabra.core

import java.io.Writer

/**
 * Writes to any number of child [Writer]s as if they were a single [Writer].  If a [Throwable] is thrown during an
 * operation on one or more child [Writer]s, the first exception will be thrown and subsequent exceptions will be
 * attached to it and suppressed.
 *
 * @param children [Writer](s) to which any writes to this [TeeWriter] will be delegated
 */
class TeeWriter(vararg children: Writer?) : Writer() {

    private val childWriters: List<Writer> = children.filterNotNull()

    override fun write(cbuf: CharArray, off: Int, len: Int) {
        childWriters.mapNotNull {
            nullOrThrowable { it.write(cbuf, off, len) }
        }.throwFirst()
    }

    override fun flush() {
        childWriters.mapNotNull {
            nullOrThrowable { it.flush() }
        }.throwFirst()
    }

    override fun close() {
        childWriters.mapNotNull {
            nullOrThrowable { it.close() }
        }.throwFirst()
    }

    fun isEmpty(): Boolean = childWriters.isEmpty()

}