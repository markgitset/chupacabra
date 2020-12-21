package net.markdrew.chupacabra.cli

import com.google.common.io.CharSink
import com.google.common.io.CharSource
import com.google.common.io.Files
import net.markdrew.chupacabra.guava.StandardInCharSource
import net.markdrew.chupacabra.guava.StandardOutCharSink
import net.markdrew.chupacabra.guava.asCharSource
import java.io.File

/**
 * Framework for common CLI applications that either reduce or convert a set of files (in parallel) 
 * from a directory (possibly recursively).
 * 
 * @param inFileToOutSink maps input [File]s to output [CharSink]s (see common implementations in companion object)
 * @param showProgress whether or not to show a progress bar on the console
 */
class FileArgsHandler(private val inFileToOutSink: (File) -> CharSink, private val showProgress: Boolean = true) {

    /**
     * Process all files in [inFilesOrDirs] in parallel (and recursively, if [recursive]) with the given [processFun]
     */
    fun <T> processArgs(
        inFilesOrDirs: List<File>, 
        recursive: Boolean = false, 
        processFun: (sourceName: String, CharSource, CharSink) -> T
    ): Sequence<T> {
        return if (inFilesOrDirs.isEmpty()) {
            sequenceOf(processFun("standard input", StandardInCharSource(), StandardOutCharSink()))
        } else {
            val files: List<File> = toFileList(inFilesOrDirs, recursive)
            files.parallelMap(showProgress) { inFile ->
                processFun(inFile.path, inFile.asCharSource(), inFileToOutSink(inFile))
            }.asSequence()
        }
    }

    @Deprecated("Use processArgs instead")
    @Suppress("unused")
    fun <T> mapArgs(
        inFilesOrDirs: List<File>, 
        recursive: Boolean = false, 
        mapFun: (sourceName: String, CharSource, CharSink) -> T
    ): Map<String, T> {
        return if (inFilesOrDirs.isEmpty()) {
            mapOf("standard input" to mapFun("standard input", StandardInCharSource(), StandardOutCharSink()))
        } else {
            val files: List<File> = toFileList(inFilesOrDirs, recursive)
            files.parallelMap(showProgress) { inFile ->
                inFile.path to mapFun(inFile.path, Files.asCharSource(inFile, Charsets.UTF_8), inFileToOutSink(inFile))
            }.toMap()
        }
    }

    companion object {

        /**
         * Returns a file-mapping function that maps input files to an [outDir], if given, or to standard output if [outDir]
         * is null.
         */
        fun inFileToOutDirOrStdOut(outDir: File?, appendSuffix: String = "", removeSuffix: String = ""): (File) -> CharSink =
            if (outDir == null) inFileToStdOut() else inFileToOutDir(outDir, appendSuffix, removeSuffix)

        /**
         * Returns a file-mapping function that maps all input files to standard output
         */
        fun inFileToStdOut(): (File) -> CharSink {
            // doing this here, outside of the returned function avoids creating a new stdout char sink for each file
            val stdOut = StandardOutCharSink()
            return { _ -> stdOut }
        }

        /**
         * Drops [removeSuffix] from [str] (if present) and adds [appendSuffix]
         */
        fun amendSuffix(str: String, appendSuffix: String = "", removeSuffix: String = ""): String {
            val trimmed = if (str.endsWith(removeSuffix)) str.dropLast(removeSuffix.length) else str
            return trimmed + appendSuffix
        }

        /**
         * Builds a file-mapping function that optionally amends the suffix of the input file name 
         * and writes to the given [outDir].
         */
        fun inFileToOutDir(outDir: File, appendSuffix: String = "", removeSuffix: String = ""): (File) -> CharSink {
            require(!outDir.isFile) { "Output directory, '$outDir', must either be a directory or not exist!" }
            if (!outDir.exists()) outDir.mkdirs()
            return { inFile: File -> 
                val outFileName = amendSuffix(inFile.name, appendSuffix, removeSuffix)
                Files.asCharSink(outDir.resolve(outFileName), Charsets.UTF_8) 
            }
        }

        /**
         * Converts a list of files and/or directories into a list of files.  If [recursive], directories are recursively expanded
         * into files. All files/directories that begin with '.' are ignored.
         */
        fun toFileList(inputFilesAndDirs: List<File>, recursive: Boolean = false): List<File> = 
            inputFilesAndDirs.flatMap { fileOrDir ->
                fileOrDir.walk()
                    .maxDepth(if (recursive) Int.MAX_VALUE else 1)
                    .onEnter { it.name[0] != '.' }
                    .filter { it.isFile && it.name[0] != '.' }.toList() 
            }

   }

}