package net.markdrew.chupacabra.core

/**
 * Comparator that places a specific value last, leaving the order of all other values unspecified.
 */
private fun <T> lastValueComparator(lastValue: T): Comparator<T> = Comparator { item1, item2 ->
    when {
        item1 == lastValue && item2 != lastValue -> 1
        item2 == lastValue && item1 != lastValue -> -1
        else -> 0
    }
}

/**
 * Modify this [Comparator] such that the given value will always be last.
 */
fun <T> Comparator<T>.withLastValue(lastValue: T): Comparator<T> = lastValueComparator(lastValue).then(this)