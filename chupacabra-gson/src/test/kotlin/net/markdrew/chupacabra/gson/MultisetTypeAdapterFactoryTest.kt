package net.markdrew.chupacabra.gson

import com.google.common.collect.HashMultiset
import com.google.common.collect.Multiset
import com.google.gson.GsonBuilder
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class MultisetTypeAdapterFactoryTest {

    internal class A {
        val a: Multiset<String> = HashMultiset.create<String>()
        override fun equals(other: Any?): Boolean = a == (other as A).a
        override fun hashCode(): Int = throw RuntimeException("Not implemented")
    }

    @Test
    fun testEmptyMultiset() {
        val gson = GsonBuilder().registerTypeAdapterFactory(MultisetTypeAdapterFactory(true)).create()

        val jsonString = "{}"
        //JsonTestUtils.makeJsonTestFor(multiset, type, gson);

        val copiedA = JsonTestUtils.testJsonAndAssertEqual(A(), jsonString, A::class.java, gson)
        assertNotNull(copiedA)
    }

    @Test
    fun testBareEmptyMultimap() {
        val a = HashMultiset.create<String>()
        val gson = GsonBuilder().registerTypeAdapterFactory(MultisetTypeAdapterFactory(false)).create()
        val suppressedGson = GsonBuilder().registerTypeAdapterFactory(MultisetTypeAdapterFactory(true)).create()
        val aType = typeOf<Multiset<String>>()
        JsonTestUtils.testRoundTrip<Multiset<String>>(a, aType, gson)
        JsonTestUtils.testRoundTrip<Multiset<String>>(a, aType, suppressedGson)
    }

    @Test
    fun testExplicitEmptyMultiset() {
        val gson = GsonBuilder().registerTypeAdapterFactory(MultisetTypeAdapterFactory(false)).create()

        val jsonString = "{\"a\":[]}"
        //JsonTestUtils.makeJsonTestFor(multiset, type, gson);

        val copiedA = JsonTestUtils.testJsonAndAssertEqual(A(), jsonString, A::class.java, gson)
        assertNotNull(copiedA)
    }

    @Test
    fun testMultiset() {
        val multiset = HashMultiset.create<String>()
        multiset.add("hi")
        multiset.add("there")
        multiset.add("hi")
        //System.out.println(multiset);

        val gson = GsonBuilder().registerTypeAdapterFactory(MultisetTypeAdapterFactory()).create()

        val jsonString = "[2,\"hi\",1,\"there\"]"
        val type = typeOf<Multiset<String>>()
        //JsonTestUtils.makeJsonTestFor(multiset, type, gson);
        JsonTestUtils.testJsonAndAssertEqual<Multiset<String>>(multiset, jsonString, type, gson)
    }

}
