package net.markdrew.chupacabra.gson

import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Test
import net.markdrew.chupacabra.core.DisjointRangeSet

class DisjointRangeSetAdapterTest {

    @Test
    fun testJson() {
        val set = DisjointRangeSet(1..3, 4..10)
        //JsonTestUtils.makeJsonTestFor(lang, Language.class);
        val jsonRange = """
            [
              "1_3",
              "4_10"
            ]
        """.trimIndent()
        val gson = GsonBuilder().registerTypeAdapter(IntRange::class.java, IntRangeAdapter("_", inclusiveEndPoints = true))
                                .registerTypeAdapter(DisjointRangeSet::class.java, DisjointRangeSetAdapter()).create()
        JsonTestUtils.testJson(set, jsonRange, DisjointRangeSet::class.java, gson)
    }

}
