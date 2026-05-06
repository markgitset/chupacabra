package net.markdrew.chupacabra.core

import mu.KLogger
import mu.KotlinLogging
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.ln1p

private val logger: KLogger = KotlinLogging.logger {}

/**
 * Adds a JVM shutdown hook that calls the given [shutdownFun] on the receiver in a new [Thread] with 'this' set to the receiver.
 *
 * @return the original receiver (for call chaining)
 */
fun <T> T.onShutdown(shutdownFun: T.() -> Unit): T {
    Runtime.getRuntime().addShutdownHook(Thread { shutdownFun() })
    return this
}

/**
 * Adds a JVM shutdown hook that closes the receiver in a new [Thread] when the JVM terminates.
 *
 * @param resourceName a descriptive name of the resource to be closed
 * @return the original receiver (for call chaining)
 */
fun <T : AutoCloseable> T.closeOnShutdown(resourceName: String = toString()): T = this.onShutdown {
    try {
        logger.info("Shutting down $resourceName...")
        close()
    } catch (e: Exception) {
        logger.error("Encountered a problem closing $resourceName.", e)
    }
}

private val ESCAPE_CHARS: CharArray = arrayOf('\\', '\n', '"').toCharArray()

/**
 * Escapes a string value for use as a literal raw string in Kotlin. Useful for Kotlin code generation tasks.
 */
fun quotedKotlinRawString(s: String?): String = when {
    s == null ->
        "null"
    s.indexOfAny(ESCAPE_CHARS) >= 0 ->
        s.let { "\"\"\"" + it.replace("$", "\${'$'}").replace("\"\"\"", "\"\"\${'\"'}") + "\"\"\"" }
    else ->
        """"$s""""
}

/**
 * Escapes a string value for use as a literal ordinary string in Kotlin. Useful for Kotlin code generation tasks.
 */
fun quotedKotlinString(s: String?): String = when {
    s == null ->
        "null"
    s.indexOfAny(ESCAPE_CHARS) >= 0 ->
        s.let { "\"" + it.replace("\n", "\\n").replace("\"", "\\\"") + "\"" }
    else ->
        """"$s""""
}

/**
 * Computes the logistic function
 */
fun logistic(x: Double, midPoint: Double = 0.0, maxValue: Double = 1.0, growthRate: Double = 1.0): Double =
    // this would be simpler, but it's more susceptible to overflow problems:
    //     exp(growthRate*(x - midPoint)).let { maxValue * it / (it + 1) }
    exp(ln(maxValue) - ln1p(exp(-growthRate*(x - midPoint))))

/**
 * Computes the logistic function
 */
fun logit(p: Double): Double = -ln(1.0 / p - 1.0)
