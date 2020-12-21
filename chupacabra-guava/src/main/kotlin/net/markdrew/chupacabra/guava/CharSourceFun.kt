package net.markdrew.chupacabra.guava

import com.google.common.io.ByteSource
import com.google.common.io.CharSource
import com.google.common.io.Files
import java.io.File
import java.nio.charset.Charset

fun File.asCharSource(charset: Charset = Charsets.UTF_8): CharSource = Files.asCharSource(this, charset)

fun File.asByteSource(): ByteSource = Files.asByteSource(this)