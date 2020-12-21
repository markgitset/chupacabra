package net.markdrew.chupacabra.guava

import com.google.common.collect.TreeBasedTable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TableHtmlFormatTest {
    
    @Test
    fun `basic format`() {

        val table = TreeBasedTable.create<String, String, Int>()
        table.put("row1", "col2", 4)
        table.put("row2", "O", 300)
        table.put("row2", "col1", 4999)
        
        val tableString: String = TableHtmlFormat<String, String, Int?>("caption", "COLS", "ROWS").apply {
            formatHeaderFun = { if (it == "O") "Other" else it }
        }.format(table)
        
        println(tableString)
        
        assertEquals("""
            <table>
            <caption>caption</caption>
            <thead>
                <tr><th>ROWS \ COLS</th><th>Other</th><th>col1</th><th>col2</th></tr>
            </thead>
            <tbody>
                <tr><th>row1</th><td>null</td><td>null</td><td>4</td></tr>
                <tr><th>row2</th><td>300</td><td>4999</td><td>null</td></tr>
            </tbody>
            </table>
        """.trimIndent(), tableString)
    }
    
    @Test
    fun `summary row`() {
        val table = TreeBasedTable.create<String, String, Int>()
        table.put("row1", "c1", 2)
        table.put("row2", "O", 3)
        table.put("row2", "c1", 49)
        table.put("row3", "c1", 3)
        table.put("row1", "c2", 2)
        table.put("row3", "c2", 3)
        val tableString: String = TableHtmlFormat<String, String, Int>("<>").apply {
            formatValueFun = { it ?: "&" }
            formatHeaderFun = { if (it == "O") "Ot&her" else it }
            //summarizeCols("TOTAL") { it.sum() }
        }.format(table)
        //println(tableString)
        assertEquals("""
            <table>
            <caption>&lt;&gt;</caption>
            <thead>
                <tr><th> \ </th><th>Ot&amp;her</th><th>c1</th><th>c2</th></tr>
            </thead>
            <tbody>
                <tr><th>row1</th><td>&amp;</td><td>2</td><td>2</td></tr>
                <tr><th>row2</th><td>3</td><td>49</td><td>&amp;</td></tr>
                <tr><th>row3</th><td>&amp;</td><td>3</td><td>3</td></tr>
            </tbody>
            </table>
        """.trimIndent(), tableString)
    }

}