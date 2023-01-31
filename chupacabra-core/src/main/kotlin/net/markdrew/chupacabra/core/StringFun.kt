package net.markdrew.chupacabra.core

import kotlin.math.min

/**
 * Outputs a grammatically correct string for [count] items.  When [count] is 1, uses the given [singularLabel].
 * When [count] is not 1, it uses the given [pluralLabel], if present, or [singularLabel] with an 's' appended, otherwise.
 */
fun plural(count: Int, singularLabel: String, pluralLabel: String = "${singularLabel}s"): String =
    "$count " + if (count == 1) singularLabel else pluralLabel

fun lengthOfCommonPrefix(s: String, t: String): Int =
    (s zip t).indexOfFirst { (u, v) -> u != v }

/**
 * Computes the Levenshtein distance between two strings using the "iterative with two matrix rows" algorithm referred
 * to in the "Levenshtein distance" Wikipedia article.
 */
fun levenshtein(
    s: String, t: String,
    charScore: ((Char, Char) -> Int)? = null // it's a bit faster to not put a default impl here
): Int {
    
    // NOTE this implementation, which I copied from Rosetta Code and then tweaked a bit,
    // appears to be a little faster than most around (up to 2x).
    
    // degenerate cases
    if (s == t) return 0
    if (s == "") return t.length
    if (t == "") return s.length
    
    // create two int arrays of distances and initialize the first one
    var v0 = IntArray(t.length + 1) { it } // previous
    var v1 = IntArray(t.length + 1)        // current
    
    for (i in s.indices) {
        // calculate v1 from v0
        v1[0] = i + 1
        for (j in t.indices) {
            val cost = charScore?.invoke(s[i], t[j]) ?: if (s[i] == t[j]) 0 else 1
            v1[j + 1] = min(v1[j] + 1, min(v0[j + 1] + 1, v0[j] + cost))
        }
        // "copy" (swap, really) v1 to v0 for next iteration
        if (i + 1 < s.length) {
            val tmp = v0; v0 = v1; v1 = tmp
        }
    }
    return v1[t.length]
}

/**
 * Returns the substring before the [n]th occurrence of [delimiter]. If [n] is negative, returns the substring before
 * the N+1-[n]th occurrence of [delimiter], where N is the total number of delimiters present.
 * If the string does not contain enough [delimiter]s and [n] > 0, returns [notEnoughFromLeftValue],
 * which defaults to the original string.
 * If the string does not contain enough [delimiter]s and [n] < 0, returns [notEnoughFromRightValue],
 * which defaults to the empty string.
 */
public fun String.substringBeforeNth(
    delimiter: String,
    n: Int,
    notEnoughFromLeftValue: String = this,
    notEnoughFromRightValue: String = ""
): String {
    require(n != 0) { "Finding the 0th occurrence of '$delimiter' doesn't make sense. Use an n != 0." }
    if (n > 0) {
        var startIndex = 0
        var nRemaining = n
        while (true) {
            val index = indexOf(delimiter, startIndex)
            if (index == -1) return notEnoughFromLeftValue
            if (nRemaining == 1) return substring(0, index)
            nRemaining--
            startIndex = index + delimiter.length
        }
    } else {
        var startIndex = lastIndex
        var nRemaining = n
        while (true) {
            val index = lastIndexOf(delimiter, startIndex)
            if (index == -1) return notEnoughFromRightValue
            if (nRemaining == -1) return substring(0, index)
            nRemaining++
            startIndex = index - delimiter.length
        }
    }
}

/**
 * Returns the substring after the [n]th occurrence of [delimiter]. If [n] is negative, returns the substring after
 * the N+1-[n]th occurrence of [delimiter], where N is the total number of delimiters present.
 * If the string does not contain enough [delimiter]s and [n] > 0, returns [notEnoughFromLeftValue],
 * which defaults to the empty string.
 * If the string does not contain enough [delimiter]s and [n] < 0, returns [notEnoughFromRightValue],
 * which defaults to the original string.
 */
public fun String.substringAfterNth(
    delimiter: String,
    n: Int,
    notEnoughFromLeftValue: String = "",
    notEnoughFromRightValue: String = this
): String {
    require(n != 0) { "Finding the 0th occurrence of '$delimiter' doesn't make sense. Use an n != 0." }
    if (n > 0) {
        var startIndex = 0
        var nRemaining = n
        while (true) {
            val index = indexOf(delimiter, startIndex)
            if (index == -1) return notEnoughFromLeftValue
            if (nRemaining == 1) return substring(index + delimiter.length)
            nRemaining--
            startIndex = index + delimiter.length
        }
    } else {
        var startIndex = lastIndex
        var nRemaining = n
        while (true) {
            val index = lastIndexOf(delimiter, startIndex)
            if (index == -1) return notEnoughFromRightValue
            if (nRemaining == -1) return substring(index + delimiter.length)
            nRemaining++
            startIndex = index - delimiter.length
        }
    }
}
