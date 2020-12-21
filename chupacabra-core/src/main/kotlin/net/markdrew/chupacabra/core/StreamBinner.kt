package net.markdrew.chupacabra.core

import kotlin.math.ulp

/**
 * Automatically builds a histogram from an unknown stream of doubles
 *
 * Experimental.  Still needs work.  Seems to produce a lot of empty buckets toward the end of the range.
 */
class StreamBinner(val numOfBins: Int) {

    private lateinit var binner: Binner

    fun bin(number: Double) {
        if (!::binner.isInitialized) binner = Binner(numOfBins, number.ulp, number)
        val binNo = binner.binFor(number)
        if (binNo == null) binner = binner.newBinnerFor(number)
        binner.counts[binner.binFor(number) ?: error("Shouldn't happen")]++
        if (binner.counts.last() == 0) {
            println("  After $number there are trailing empty bins")
        }
        if (binner.counts.first() == 0) {
            println("  After $number there are leading empty bins")
        }
    }

    override fun toString(): String = if (::binner.isInitialized) binner.toString() else "nothing yet"

    fun bin(numberSeq: Sequence<Double>) {
        numberSeq.forEach { bin(it) }
    }

}

fun main() {

//    val s: Sequence<Double> = sequenceOf(0.01, -1200.0, 100.0, /*1.0e10,*/ Math.PI)
    val binner = StreamBinner(10)
    fun binAndPrint(num: Double) {
        println("\nAdding $num")
        binner.bin(num)
        println(binner)
    }

//    binner.bin(s)
//    println()
//    println(binner)
//
////    binAndPrint(10.0)
////    binAndPrint(0.01)
////    binAndPrint(-1200.0)
////    binAndPrint(200.0)
    binAndPrint(100.0)
    binAndPrint(0.701)
    binAndPrint(-12.0)
    binAndPrint(-200.0)

//    generateSequence { Random.nextDouble(-1000000.0, 1000000.0) }.take(10).forEach {
//        println(it)
//        binner.bin(it)
//    }
//    println(binner)
}

private class Binner(val numOfBins: Int, val binWidth: Double, minIncluded: Double) {

    init {
        require(binWidth > 0.0)
    }

    // the last element of this array is actually an upper bound, which we include for convenience
    val lowerBounds = DoubleArray(numOfBins + 1) { minIncluded + it*binWidth }
    val counts = IntArray(numOfBins)

    fun binFor(number: Double): Int? = if (number > lowerBounds[numOfBins]) {
        null // number's too big for current bins
    } else {
        lowerBounds.indexOfLast { number >= it }.takeUnless { it < 0 }
    }

    // for use when number isn't in range of this Binner
    fun newBinnerFor(number: Double): Binner {
        if (binWidth < Double.MIN_VALUE) {
            return Binner(numOfBins, number.ulp, number)
        } else {
            val idealMin = minOf(lowerBounds[0], number)
            val idealMax = maxOf(lowerBounds[numOfBins], number)
//            val idealMax = maxOf(lowerBounds[counts.indexOfLast { it > 0 } + 1], number)
            val idealBinWidth: Double = (idealMax - idealMin) / numOfBins
            // we'd just use these ideal values, but we want to be able to neatly re-bin old values into the new bins, so
            // we'll make adjustments to ensure that no old bins are split across new bins

            val binScaleFactor: Double = idealBinWidth / binWidth
            if (binScaleFactor > 100*numOfBins) { // new bins are vastly larger
                // compute parameters for, and instantiate, the new Binner
                val newBinWidth = (idealMax - idealMin) / (numOfBins - 1)
//                val newBinWidth = idealBinWidth + numOfBins*binWidth*10//* (1.0 + 0.01*idealBinWidth/numOfBins)
                var newMinIncluded = lowerBounds[0]// - newBinWidth/2.0
                while (newMinIncluded > idealMin) newMinIncluded -= newBinWidth
                val newBinner = Binner(numOfBins, newBinWidth, newMinIncluded)

                // copy counts from this binner into the new binner
                for (i in 0 until numOfBins) {
                    if (counts[i] == 0) continue
                    val newIndex = newBinner.binFor(lowerBounds[i]/* + binWidth/2.0*/)
                        ?: error("Bin $i ${lowerBounds[i]} doesn't fit in binner with range ${newBinner.lowerBounds[0]}, ${newBinner.lowerBounds.last()}")
                    newBinner.counts[newIndex] += counts[i]
                }
                return newBinner

            } else { // new bins are relatively similar sizes

                // new bin widths will be this factor times old bin widths
                val intBinScaleFactor: Long = if (binWidth < Double.MIN_VALUE) 1 else binScaleFactor.toLong() + 1

                // compute parameters for, and instantiate, the new Binner
                val newBinWidth = intBinScaleFactor * binWidth
                var newMinIncluded = lowerBounds[numOfBins]
                while (newMinIncluded > idealMin) newMinIncluded -= newBinWidth
                val newBinner = Binner(numOfBins, newBinWidth, newMinIncluded)

                // copy counts from this binner into the new binner
                for (i in 0 until numOfBins) {
                    if (counts[i] == 0) continue
                    val newIndex = newBinner.binFor(lowerBounds[i]/* + binWidth/2.0*/)
                        ?: error("Bin $i ${lowerBounds[i]} doesn't fit in binner with range ${newBinner.lowerBounds[0]}, ${newBinner.lowerBounds.last()}")
                    newBinner.counts[newIndex] += counts[i]
                }
                return newBinner
            }
        }
    }

    override fun toString(): String =
        (lowerBounds zip counts.toTypedArray()).joinToString("\n") { (lowerBound, count) ->
            "$lowerBound - ${lowerBound + binWidth}: $count"
        }

}

