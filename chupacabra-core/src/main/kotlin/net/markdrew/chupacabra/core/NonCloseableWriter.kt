package net.markdrew.chupacabra.core

import java.io.IOException
import java.io.Writer
import java.nio.charset.Charset

/**
 * Wraps a [Writer] so that when its [close] method is called, it will only flush the wrapped [Writer], instead of closing it.
 * The primary use case for this class is when you want to treat standard error or standard out like
 * any other file, but you don't want them to be closed when you would normally close the file writer.
 */
class NonCloseableWriter(private val delegate: Writer) : Writer() {

    @Throws(IOException::class)
    override fun write(charBuffer: CharArray, off: Int, len: Int) {
        delegate.write(charBuffer, off, len)
    }

    @Throws(IOException::class)
    override fun close() {
        flush()
    }

    @Throws(IOException::class)
    override fun flush() {
        delegate.flush()
    }

    companion object {

        /**
         * @return a new [NonCloseableWriter] that delegates to [System.out].
         */
        fun stdout(charset: Charset = Charsets.UTF_8) = NonCloseableWriter(System.out.writer(charset))

        /**
         * @return a new [NonCloseableWriter] that delegates to [System.err].
         */
        fun stderr(charset: Charset = Charsets.UTF_8) = NonCloseableWriter(System.err.writer(charset))

    }

}