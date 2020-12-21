package net.markdrew.chupacabra.core

@Suppress("MemberVisibilityCanPrivate")
data class Fraction(val numerator: Int, val denominator: Int) : Number() {
    override fun toByte(): Byte = toDouble().toByte()
    override fun toChar(): Char = toDouble().toChar()
    override fun toDouble(): Double = numerator.toDouble() / denominator
    override fun toFloat(): Float = toDouble().toFloat()
    override fun toInt(): Int = toDouble().toInt()
    override fun toLong(): Long = toDouble().toLong()
    override fun toShort(): Short = toDouble().toShort()
    override fun toString(): String = "$numerator/$denominator"

    operator fun times(factor: Fraction): Fraction = Fraction(numerator * factor.numerator, denominator * factor.denominator)
    operator fun times(factor: Double): Double = toDouble() * factor

    operator fun plus(addend: Fraction): Fraction =
        if (denominator == addend.denominator) Fraction(numerator + addend.numerator, denominator)
        else Fraction(numerator * addend.denominator + addend.numerator * denominator, denominator * addend.denominator)
    
    operator fun plus(addend: Double): Double = toDouble() + addend

    fun weightedAverage(other: Fraction): Fraction = Fraction(numerator + other.numerator, denominator + other.denominator)
    
    companion object {
        operator fun Double.times(factor: Fraction): Double = factor * this
        operator fun Double.plus(factor: Fraction): Double = factor + this
        fun Iterable<Fraction>.weightedAverage(): Fraction = reduce { acc, fraction -> acc.weightedAverage(fraction) }
    }

}

//operator fun Double.times(factor: Fraction): Double = this * factor.toDouble()
