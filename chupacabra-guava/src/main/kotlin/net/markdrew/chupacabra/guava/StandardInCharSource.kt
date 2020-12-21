package net.markdrew.chupacabra.guava

import com.google.common.io.CharSource
import java.io.Reader
import java.nio.charset.Charset

class StandardInCharSource(charset: Charset = Charsets.UTF_8) : CharSource() {

    private val reader: Reader = System.`in`.reader(charset)
    
    /**
     * Opens a new [Reader] for reading from this source. This method returns a new, independent
     * reader each time it is called.
     *
     * The caller is responsible for ensuring that the returned reader is closed.
     *
     * @throws IOException if an I/O error occurs while opening the reader
     */
    override fun openStream(): Reader = reader

    override fun toString(): String = this::class.java.simpleName

}