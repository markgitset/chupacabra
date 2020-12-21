package net.markdrew.chupacabra.core

/**
 * Computes the string subsequence kernel (SSK) for two strings. The SSK is the
 * inner product of two strings in the string subsequence space, where a
 * subsequence is a sequence of not-necessarily contiguous characters within
 * another sequence.
 *
 * See "Text Classification using String Kernels" by Huma Lodhi, et al. in
 * Journal of Machine Learning Research (2002) for more information.
 */
class StringSubsequenceKernel
/**
 * Construct a new string subsequence kernel function with the given decay
 * factor (lambda, defaults to {@value #DEFAULT_LAMBDA}). Lambda must be in 
 * the range (0,1]. The smaller this value is, the more non-contiguous 
 * subsequences are penalized. Setting lambda to 1.0 eliminates the penalty for 
 * non-contiguous subsequences.
 *
 * @param lambda decay factor in the range (0,1]. The smaller this value is, 
 * the more non-contiguous subsequences are penalized. Setting lambda to 1.0
 * eliminates the penalty for non-contiguous subsequences.
 */
@JvmOverloads constructor(private val lambda: Double = 0.5) {

    /*
     * TODO add support for caching (especially for normalization)?
     * TODO can this be optimized when s == t (especially for normalization)?
     * TODO support retaining/returning intermediate values
     * TODO change to use char[] instead of String for performance?
     * TODO refactor remaining recursion as loops for performance
     * TODO rawKernel and kPrime are VERY similar--consolidate somehow?
     */

    /**
     * Maximum cache size.  Default value is {@value #DEFAULT_CACHE_SIZE}.
     */
    var cacheSize = 1_000

    /**
     * Cache for raw kernels of strings with themselves
     */
    @Transient 
    private val caches: Array<MutableMap<String, Double>?> = arrayOfNulls(5)

    init {
        require(lambda > 0.0) { "lambda must be greater than zero" }
        require(lambda <= 1.0) { "lambda must be less than or equal to one" }
    }

    /**
     * Returns the SSK, normalized by the string lengths. The SSK is computed
     * between s and t in the space of subsequences whose length is n. A result
     * of 0.0 means the arguments are very dissimilar and 1.0 means the
     * arguments are exactly similar.
     *
     * @param subseqLen character length of subsequences to consider
     * @param s a string
     * @param t another string
     * @return the SSK, normalized by the string lengths
     */
    fun normalizedKernel(subseqLen: Int, s: String, t: String): Double {
        val raw = rawKernel(subseqLen, s, t)
        // This check makes the computation more efficient and avoids dividing by zero when |s| or |t| < n.
        return if (raw < 0.0000001) 0.0 else raw / Math.sqrt(rawKernel(subseqLen, s) * rawKernel(subseqLen, t))
    }

    private fun rawKernel(subseqLen: Int, s: String): Double {
        if (subseqLen - 1 >= caches.size) {
            return rawKernel(subseqLen, s, s)
        }
        var cache: MutableMap<String, Double>? = caches[subseqLen - 1]
        if (cache == null) {
            cache = CacheMap(cacheSize)
            caches[subseqLen - 1] = cache
        }
        val value = cache[s]
        return if (value == null) {
            val v = rawKernel(subseqLen, s, s)
            cache[s] = v
            v
        } else {
            value
        }
    }

    /**
     * Returns the non-normalized SSK of s and t in the space of subsequences
     * whose length is n.
     *
     * @param subseqLen character length of subsequences to consider
     * @param s a string
     * @param t another string
     * @return the SSK of s and t in the space of subsequences whose length is n
     * @see .normalizedKernel
     */
    fun rawKernel(subseqLen: Int, s: String, t: String): Double {
        require(subseqLen > 0) { "Subsequence length must be greater than zero" }
        val nMinusOne = subseqLen - 1
        val sLength = s.length
        val tLength = t.length
        var rawKernel = 0.0
        for (i in nMinusOne until sLength) {
            val sPrefix = s.substring(0, i)
            val sSuffix = s[i]
            for (j in nMinusOne until tLength) {
                if (t[j] == sSuffix) {
                    //println("  rawKernel += " + kPrime(nMinusOne, sPrefix, t.substring(0, j)))
                    rawKernel += kPrime(nMinusOne, sPrefix, t.substring(0, j))
                }
            }
        }
        return rawKernel
    }

    /**
     * Computes K'n(s,t), where s is a string whose last character is x and t
     * is another string.
     *
     * @param n character length of subsequences to consider
     * @param s a string
     * @param t another string
     * @return K'n(s,t)
     */
    private fun kPrime(n: Int, s: String, t: String): Double {
        // by definition kPrime = 1.0 when n = 0
        if (n == 0) return 1.0

        val nMinusOne = n - 1
        val sLength = s.length
        val tLength = t.length
        var sum = 0.0
        for (i in nMinusOne until sLength) {
            // split s into sPrefix and sSuffix
            val sPrefix = s.substring(0, i)
            val sSuffix = s[i]

            var kPrimePrime = 0.0
            for (j in nMinusOne until tLength) {
                if (t[j] == sSuffix) {
                    kPrimePrime += lambda * kPrime(
                        nMinusOne,
                        sPrefix, t.substring(0, j)
                    )
                    //println("      kPrimePrime += $lambda * ${kPrime(nMinusOne, sPrefix, t.substring(0, j))}")
                }
                //println("      kPrimePrime *= $lambda")
                kPrimePrime *= lambda
            }
            //println("    sum = $lambda * $sum + $kPrimePrime")
            sum = lambda * sum + kPrimePrime
        }
        return sum
    }

}
