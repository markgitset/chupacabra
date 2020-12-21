package net.markdrew.chupacabra.gson

import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Test

class IntRangeAdapterTest {

    @Test
    fun testJson() {
        val range = 1..9
        //JsonTestUtils.makeJsonTestFor(lang, Language.class);
        val jsonRange = "1_9"
        val adapter = IntRangeAdapter("_", inclusiveEndPoints = true)
        val gson = GsonBuilder().registerTypeAdapter(IntRange::class.java, adapter).create()
        JsonTestUtils.testJson(range, jsonRange, IntRange::class.java, gson)
    }

    @Test
    fun testJsonDefaultDelimiter() {
        val range = 1..9
        //JsonTestUtils.makeJsonTestFor(lang, Language.class);
        val jsonRange = "1..10"
        val gson = GsonBuilder().registerTypeAdapter(IntRange::class.java, IntRangeAdapter()).create()
        JsonTestUtils.testJson(range, jsonRange, IntRange::class.java, gson)
    }

}
