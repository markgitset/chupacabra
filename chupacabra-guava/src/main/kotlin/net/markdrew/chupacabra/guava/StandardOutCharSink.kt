package net.markdrew.chupacabra.guava

import com.google.common.io.CharSink
import net.markdrew.chupacabra.core.NonCloseableWriter
import java.io.Writer
import java.nio.charset.Charset

class StandardOutCharSink(charset: Charset = Charsets.UTF_8) : CharSink() {
    
    private val writer: Writer = NonCloseableWriter.stdout(charset)
    
    /**
     * Opens a new [Writer] for writing to this sink. This method returns a new, independent
     * writer each time it is called.
     *
     * The caller is responsible for ensuring that the returned writer is closed.
     */
    override fun openStream(): Writer = writer

    override fun toString(): String = this::class.java.simpleName
    
}