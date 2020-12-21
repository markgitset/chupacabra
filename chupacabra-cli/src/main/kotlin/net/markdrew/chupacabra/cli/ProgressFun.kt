package net.markdrew.chupacabra.cli

import me.tongfei.progressbar.ProgressBar
import me.tongfei.progressbar.ProgressBarStyle.COLORFUL_UNICODE_BLOCK
import net.markdrew.chupacabra.core.parallelMap

/**
 * Runs the given function, [f], on each element of this collection in parallel.  After all elements have been processed, returns
 * the mapped collection.
 *
 * @param showProgress true to show a progress bar, false otherwise
 */
fun <T, U> Collection<T>.parallelMap(showProgress: Boolean, f: suspend (T) -> U): List<U> = if (showProgress) {
    parallelMapWithProgress { f(it) }
} else {
    parallelMap { f(it) }
}

/**
 * Runs the given function, [f], on each element of this collection in parallel and shows a progress bar while processing.  
 * After all elements have been processed, returns the mapped collection.
 */
fun <T, U> Collection<T>.parallelMapWithProgress(f: suspend (T) -> U): List<U> =
    ProgressBar("processing files", size.toLong(), 500, System.err, COLORFUL_UNICODE_BLOCK, " files", 1, true).use { pb ->
        parallelMap({ pb.step() }) { f(it) }
    }
