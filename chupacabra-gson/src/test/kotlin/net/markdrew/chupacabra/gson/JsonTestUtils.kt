package net.markdrew.chupacabra.gson

import com.google.common.escape.CharEscaperBuilder
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import org.junit.jupiter.api.Assertions.assertEquals
import java.lang.reflect.Type

object JsonTestUtils {

    private fun defaultGson(): Gson = GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping() // avoids '<' being rendered as '\u003c', e.g.
            .create()

    /**
     * Used to help write new unit tests (not for running tests)
     */
    @Suppress("UNUSED")
    fun makeJsonTestFor(obj: Any, type: Type): String = makeJsonTestFor(
            obj, type, defaultGson()
    )

    /**
     * Used to help write new unit tests (not for running tests)
     */
    fun makeJsonTestFor(obj: Any, type: Type, gson: Gson): String {
        val escaper = CharEscaperBuilder().addEscape('"', "\\\"").addEscape('\n', "\" +\n\"").toEscaper()
        val jsonString = escaper.escape(gson.toJson(obj, type))

        val sb = StringBuilder()
        sb.append("    @Test\n")
        sb.append("    fun testJson() {\n")
        sb.append("        ").append(type.typeName).append(" testSubject = null\n")
        sb.append("        val jsonString: String = \"").append(jsonString).append("\"\n")
        sb.append("        JsonTestUtils.testJson(testSubject, jsonString, ").append(type.typeName).append("::class.java)\n")
        sb.append("    }\n")
        println(sb)
        return sb.toString()
    }

    private val prettyGson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    fun pretty(json: String): String = pretty(JsonParser().parse(json))

    fun pretty(json: JsonElement): String = prettyGson.toJson(json)

    fun <T> testRoundTrip(obj: T, type: Type, gson: Gson) {
        val jsonString = gson.toJson(obj, type)
        //System.out.println(jsonString);
        assertEquals(obj, gson.fromJson<Any>(jsonString, type))
    }

    /**
     * @return copy of the given obj after serialization/deserialization
     */
    fun <T> testJsonAndAssertEqual(obj: T, expectedJsonString: String, type: Type, gson: Gson): T {
        val fromJson = testJson(obj, expectedJsonString, type, gson)
        assertEquals(obj, fromJson)
        return fromJson
    }

    fun <T> testJson(obj: T, expectedJsonString: String, type: Type, gson: Gson): T {
        val jsonObj = pretty(gson.toJsonTree(obj, type))
        assertEquals(pretty(expectedJsonString), jsonObj)
        return gson.fromJson<T>(jsonObj, type)
    }
}
