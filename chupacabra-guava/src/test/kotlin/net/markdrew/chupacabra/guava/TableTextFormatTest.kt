package net.markdrew.chupacabra.guava

import com.google.common.collect.TreeBasedTable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class TableTextFormatTest {
    
    @Test
    fun `basic format`() {

        val table = TreeBasedTable.create<String, String, Int>()
        table.put("row1", "col2", 4)
        table.put("row2", "O", 300)
        table.put("row2", "col1", 4999)
        
        val tableString: String = TableTextFormat<String, String, Int?>().apply {
            formatHeaderFun = { if (it == "O") "Other" else it }
        }.format(table)
        
        //println(tableString)
        
        assertEquals("""
            |     |Other |col1 |col2
            |row1 | null |null |   4
            |row2 |  300 |4999 |null
        """.trimMargin(), tableString)
    }
    
    @Test
    fun `format with numbered column keys`() {

        val table = TreeBasedTable.create<String, String, Int>()
        table.put("row1", "col2", 4)
        table.put("row2", "O", 300)
        table.put("row2", "alonglongcol1", 4999)
        
        val tableString: String = TableTextFormat<String, String, Int?>().apply {
            formatHeaderFun = { if (it == "O") "Other" else it }
            colKeyFun = TableTextFormat.numberedColKeyFun()
        }.format(table)
        
        //println(tableString)
        
        assertEquals("""
            |     |   1 |   2 |   3
            |row1 |null |null |   4
            |row2 | 300 |4999 |null
            |
            |Column Key:
            |  1 = Other
            |  2 = alonglongcol1
            |  3 = col2
        """.trimMargin(), tableString)
    }
    
    @Test
    fun `format with lower letter column keys`() {

        val table = TreeBasedTable.create<String, String, Int>()
        table.put("row1", "col2", 4)
        table.put("row2", "O", 300)
        table.put("row2", "alonglongcol1", 4999)
        
        val tableString: String = TableTextFormat<String, String, Int?>().apply {
            formatHeaderFun = {
                if (it == "O") "Other" else it
            }
            colKeyFun = TableTextFormat.letteredColKeyFun()
        }.format(table)
        
        //println(tableString)
        
        assertEquals("""
            |     |   a |   b |   c
            |row1 |null |null |   4
            |row2 | 300 |4999 |null
            |
            |Column Key:
            |  a = Other
            |  b = alonglongcol1
            |  c = col2
        """.trimMargin(), tableString)
    }
    
    @Test
    fun `format with uniquely abbreviated column keys`() {

        val table = TreeBasedTable.create<String, String, Int>()
        table.put("row1", "people", 4)
        table.put("row1", "people1", 4)
        table.put("row1", "people3", 4)
        table.put("row1", "pans", 4)
        table.put("row1", "pandas", 4)
        table.put("row2", "O", 300)
        table.put("row2", "alonglongcol1", 4999)
        
        val tableString: String = TableTextFormat<String, String, Int?>().apply {
            formatHeaderFun = { if (it == "O") "Other" else it }
            colKeyFun = TableTextFormat.uniqueAbbreviatedColKeyFun(3)
        }.format(table)
        
        //println(tableString)
        
        assertEquals("""
            |     | Oth | alo | pan | pas | peo | pep | pel
            |row1 |null |null |   4 |   4 |   4 |   4 |   4
            |row2 | 300 |4999 |null |null |null |null |null
            |
            |Column Key:
            |  Oth = Other
            |  alo = alonglongcol1
            |  pan = pandas
            |  pas = pans
            |  peo = people
            |  pep = people1
            |  pel = people3
        """.trimMargin(), tableString)
    }
    
    @Test
    fun `format should be compact by default`() {
        val table = TreeBasedTable.create<String, String, String>()
        table.put("row1", "col2", "4")
        table.put("row2", "O", "3")
        table.put("row2", "col1", "     4")
        val tableString: String = TableTextFormat<String, String, String?>(maxTableWidth = 100).apply {
            formatValueFun = { it ?: "-" }
            formatHeaderFun = { if (it == "O") "Other" else it }
        }.format(table)
        //println(tableString)
        assertEquals("""
            |     |Other |col1 |col2
            |row1 |    - |   - |   4
            |row2 |    3 |   4 |   -
        """.trimMargin(), tableString)
    }
    
    @Test
    fun `cols too wide to honor max width`() {
        val table = TreeBasedTable.create<String, String, Int>()
        table.put("row1", "c2", 4)
        table.put("row2", "O", 3)
        table.put("row2", "c1", 4999)
        val tableString: String = TableTextFormat<String, String, Int?>(
            maxTableWidth = 19,
            columnsLabel = "COLS"
        ).apply {
            formatValueFun = { it ?: "-" }
            formatHeaderFun = { if (it == "O") "Other" else it }
        }.format(table)
        //println(tableString)
        assertEquals("""
            | \ COLS |Ot |  c1 |c2
            |   row1 | - |   - | 4
            |   row2 | 3 |4999 | -
        """.trimMargin(), tableString)
    }
    
    @Test
    fun `cols too wide to honor max width cells with leading space`() {
        val table = TreeBasedTable.create<String, String, String>()
        table.put("row1", "c2", "4")
        table.put("row2", "O", "3")
        table.put("row2", "c1", "     4999")
        val tableString: String = TableTextFormat<String, String, String?>(maxTableWidth = 19).apply {
            formatValueFun = { it ?: "-" }
            formatHeaderFun = { if (it == "O") "Other" else it }
        }.format(table)
        //println(tableString)
        assertEquals("""
            |     |Oth |  c1 |c2
            |row1 |  - |   - | 4
            |row2 |  3 |4999 | -
        """.trimMargin(), tableString)
    }
    
    @Test
    fun `summary row`() {
        val table = TreeBasedTable.create<String, String, Int?>()
        table.put("row1", "c1", 2)
        table.put("row2", "O", 3)
        table.put("row2", "c1", 49)
        table.put("row3", "c1", 3)
        table.put("row1", "c2", 2)
        table.put("row3", "c2", 3)
        val tableString: String = TableTextFormat<String, String, Int>().apply {
            formatValueFun = { it ?: "-" }
            formatHeaderFun = { if (it == "O") "Other" else it }
            summarizeCols("TOTAL") { it.filterNotNull().sum() }
        }.format(table)
        //println(tableString)
        assertEquals("""
            |      |Other |c1 |c2
            | row1 |    - | 2 | 2
            | row2 |    3 |49 | -
            | row3 |    - | 3 | 3
            |_____ |_____ |__ |__
            |TOTAL |    3 |54 | 5
        """.trimMargin(), tableString)
    }

}