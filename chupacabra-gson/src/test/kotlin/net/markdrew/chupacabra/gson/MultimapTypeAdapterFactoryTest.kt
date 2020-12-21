package net.markdrew.chupacabra.gson

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.HashMultimap
import com.google.common.collect.ListMultimap
import com.google.common.collect.Multimap
import com.google.common.collect.SetMultimap
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.lang.reflect.Type

class MultimapTypeAdapterFactoryTest {

    @Test
    fun testListMultimap() {
        val type = typeOf<ListMultimap<String, Int>>()
        val jsonString = "[[\"hi\",4,4],[\"there\",9,10]]"
        doTest(ArrayListMultimap.create<String, Int>(), type, jsonString)
    }

    @Test
    fun testSetMultimap() {
        val type = object : TypeToken<SetMultimap<String, Int>>() {}.type
        val jsonString = "[[\"hi\",4],[\"there\",9,10]]"
        doTest(HashMultimap.create<String, Int>(), type, jsonString)
    }

    internal class A {
        val a: Multimap<String, Int> = ArrayListMultimap.create<String, Int>()
        override fun equals(other: Any?): Boolean = a == (other as A).a
        override fun hashCode(): Int = throw RuntimeException("Not implemented")
    }

    @Test
    fun testEmptyMultimapField() {
        val jsonString = "{}"
        val gson = GsonBuilder().registerTypeAdapterFactory(MultimapTypeAdapterFactory(true)).create()
        //JsonTestUtils.makeJsonTestFor(multimap, type, gson);

        val newA = JsonTestUtils.testJsonAndAssertEqual(A(), jsonString, A::class.java, gson)
        assertNotNull(newA.a)
        JsonTestUtils.testRoundTrip(A(), A::class.java, gson)
    }

    @Test
    fun testBareEmptyMultimap() {
        val a = ArrayListMultimap.create<String, Int>()
        val gson = GsonBuilder().registerTypeAdapterFactory(MultimapTypeAdapterFactory(false)).create()
        val suppressedGson = GsonBuilder().registerTypeAdapterFactory(MultimapTypeAdapterFactory(true)).create()
        val aType = typeOf<Multimap<String, Int>>()
        JsonTestUtils.testRoundTrip<Multimap<String, Int>>(a, aType, gson)
        JsonTestUtils.testRoundTrip<Multimap<String, Int>>(a, aType, suppressedGson)
    }

    private fun doTest(multimap: Multimap<String, Int>, type: Type, jsonString: String) {
        multimap.put("hi", 4)
        multimap.put("there", 9)
        multimap.put("hi", 4)
        multimap.put("there", 10)
        //System.out.println(multimap);

        val gson = GsonBuilder().registerTypeAdapterFactory(MultimapTypeAdapterFactory()).create()
        //JsonTestUtils.makeJsonTestFor(multimap, type, gson);
        JsonTestUtils.testJsonAndAssertEqual(multimap, jsonString, type, gson)
    }

}
