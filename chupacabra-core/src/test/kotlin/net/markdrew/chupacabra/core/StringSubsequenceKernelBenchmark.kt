package net.markdrew.chupacabra.core

import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

class StringSubsequenceKernelBenchmark {
    @Test
    fun benchmark() {
        val s1 = "science is organized knowledge".repeat(2)
        val s2 = "wisdom is organized life".repeat(2)
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
        println("BENCHMARK TIME: $time ms")
    }
}
