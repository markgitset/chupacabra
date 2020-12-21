package net.markdrew.chupacabra.gson

import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Test
import net.markdrew.chupacabra.core.Fraction

class FractionAdapterTest {

    @Test
    fun testJson() {
        val fraction = Fraction(1, 9)
        //JsonTestUtils.makeJsonTestFor(fraction, Fraction::class.java)
        val jsonFraction = "1_9"
        val gson = GsonBuilder().registerTypeAdapter(Fraction::class.java, FractionAdapter("_")).create()
        JsonTestUtils.testJson(fraction, jsonFraction, Fraction::class.java, gson)
    }

    @Test
    fun testJsonDefaultDelimiter() {
        val fraction = Fraction(1, 10)
        JsonTestUtils.testJson(fraction, """"1/10"""", Fraction::class.java, gson)
    }

    companion object {
        private val gson = GsonBuilder().registerTypeAdapter(Fraction::class.java, FractionAdapter()).create()
    }
    
}
