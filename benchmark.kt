import kotlin.system.measureTimeMillis
import net.markdrew.chupacabra.core.StringSubsequenceKernel

fun main() {
    val s1 = "science is organized knowledge"
    val s2 = "wisdom is organized life"
    val kernel = StringSubsequenceKernel(0.5)

    // Warm up
    for (i in 1..4) {
        kernel.normalizedKernel(i, s1, s2)
    }

    // Benchmark
    val time = measureTimeMillis {
        for (i in 1..6) {
            kernel.normalizedKernel(i, s1, s2)
        }
    }
    println("Time: $time ms")
}
