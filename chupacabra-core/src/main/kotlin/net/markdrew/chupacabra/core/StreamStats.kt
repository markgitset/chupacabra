package net.markdrew.chupacabra.core

import kotlin.Double.Companion.NaN
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

class StreamStats {

    var count: Int = 0
        private set

    var mean: Double = NaN
        private set

    var min: Double = NaN
        private set

    var max: Double = NaN
        private set

    private var sumOfSquaresOfDiffsFromCurrentMean: Double = NaN

    fun update(x: Double): StreamStats {
        count++
        if (count == 1) {
            min = x
            max = x
            mean = x
            sumOfSquaresOfDiffsFromCurrentMean = 0.0
        } else {
            min = min(x, min)
            max = max(x, max)
            val oldMean = mean
            mean += (x - oldMean) / count
            sumOfSquaresOfDiffsFromCurrentMean += (x - mean) * (x - oldMean)
        }
        return this
    }

    fun update(otherStats: StreamStats): StreamStats {
        when {
            otherStats.count == 0 -> {
                // nothing to do
            }
            count == 0 -> {
                // just copy the otherStats into this one
                count = otherStats.count
                mean = otherStats.mean
                min = otherStats.min
                max = otherStats.max
                sumOfSquaresOfDiffsFromCurrentMean = otherStats.sumOfSquaresOfDiffsFromCurrentMean
            }
            else -> {
                // actually need to do a little work
                // (see https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#Parallel_algorithm)
                val oldCount = count
                count += otherStats.count
                min = min(otherStats.min, min)
                max = max(otherStats.max, max)
                val delta = otherStats.mean - mean
                mean += delta * otherStats.count / count
                sumOfSquaresOfDiffsFromCurrentMean +=
                    otherStats.sumOfSquaresOfDiffsFromCurrentMean + delta.pow(2) * oldCount * otherStats.count / count
            }
        }
        return this
    }

    fun sampleVariance(): Double = sumOfSquaresOfDiffsFromCurrentMean / (count - 1)

    fun populationVariance(): Double = sumOfSquaresOfDiffsFromCurrentMean / count

    fun sampleStdDev(): Double = sqrt(sampleVariance())

    fun populationStdDev(): Double = sqrt(populationVariance())

    override fun toString(): String = "n=$count, mean=%4f, range=[%4f, %4f], s²=%6f, σ²=%6f".format(
        mean, min, max, sampleVariance(), populationVariance()
    )

}

fun Sequence<Double>.stats(): StreamStats = fold(StreamStats()) { stats, x -> stats.update(x) }
fun Iterable<Double>.stats(): StreamStats = asSequence().stats()
fun Iterator<Double>.stats(): StreamStats = asSequence().stats()
