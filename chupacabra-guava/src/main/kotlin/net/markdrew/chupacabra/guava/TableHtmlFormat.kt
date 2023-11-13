package net.markdrew.chupacabra.guava

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import com.google.common.html.HtmlEscapers

/**
 * Formats a Guava [Table] as HTML
 */
class TableHtmlFormat<R : Any, C, V>(
    val caption: String = "",
    val columnsLabel: String = "",
    val rowsLabel: String = "",
    val tableClass: String? = null,
    val colsClass: String? = null
) {

    private val escaper = HtmlEscapers.htmlEscaper()
    var formatValueFun: (V?) -> Any? = { it.toString() }
    var formatHeaderFun: (Any?) -> Any? = { it.toString() }
    var rowOrder: Comparator<in R>? = null
    var colOrder: Comparator<in C>? = null
    
//    private var summarizeColLabel: String = ""
//    private var summarizeColFun: ((Collection<V>) -> V)? = null
//    fun summarizeCols(label: String, sumFun: (Collection<V>) -> V) {
//        summarizeColLabel = label
//        summarizeColFun = sumFun
//    }
    
    private fun formatValue(value: V?): String = escaper.escape(formatValueFun(value).toString().trimStart())
    private fun formatHeader(header: Any?): String = escaper.escape(formatHeaderFun(header).toString().trimStart())

    fun format(table: Table<R, C, V?>): String {
        val rowKeys = table.rowKeySet()
        val colKeys = table.columnKeySet()
        val nCols = colKeys.size
        if (nCols == 0) throw Exception("Can't format a table with no columns!")
        
        val labelsLabel = "$rowsLabel \\ $columnsLabel"
        val orderedColKeys: List<C> = colOrder?.let { colKeys.sortedWith(it) } ?: colKeys.toList()

        fun formatRow(formattedRowHeader: String, row: Map<C, V?>): String = "<th>" + formattedRowHeader +
                "</th><td>" + orderedColKeys.joinToString("</td><td>", postfix = "</td>") { colKey ->
            formatValue(row[colKey])
        }

        fun formatRow(rowKey: R, row: Map<C, V?>): String = formatRow(formatHeader(rowKey), row)

        val colClassStr = colsClass?.let { " class=\"$it\"" }.orEmpty()
        val headerRow: String = orderedColKeys.joinToString("</th><th$colClassStr>", 
                "<tr><th>${escaper.escape(labelsLabel)}</th><th$colClassStr>", "</th></tr>") { colKey -> 
                    formatHeader(colKey)
                }

//        val summaryRow: String = summarizeColFun?.let { sumFun ->
//            val horizontalLine: String = "_".repeat(rowHeaderWidth) +
//                    columnSeparator + orderedColKeys.joinToString(columnSeparator) { (_, width) -> "_".repeat(width) }
//            "\n" + horizontalLine +
//            "\n" + formatRow(summarizeColLabel, table.columnMap().mapValues { (_, map) -> sumFun(map.values) })
//        }.orEmpty()

        val orderedRowKeys: List<R> = rowOrder?.let { rowKeys.sortedWith(it) } ?: rowKeys.toList()
        val tableClassStr = tableClass?.let { " class=\"$it\"" }.orEmpty()
        return "<table$tableClassStr>\n" +
                "<caption>${escaper.escape(caption)}</caption>\n" +
                "<thead>\n    $headerRow\n</thead>\n" +
                "<tbody>\n    " +
                orderedRowKeys.joinToString("</tr>\n    <tr>", "<tr>", "</tr>") { rowKey ->
                    formatRow(rowKey, table.row(rowKey))
                } + "\n" +
                "</tbody>\n" +
                "</table>"//+ summaryRow
    }

    companion object {

        fun <R : Any, C, V> format(table: Table<R, C, V?>): String = TableHtmlFormat<R, C, V>().format(table)

        @JvmStatic
        fun main(args: Array<String>) {
            val table = HashBasedTable.create<String, String, Int>()
            table.put("row3", "col3", 3)
            table.put("row1", "col2", 4)
            table.put("row2", "O", 300)
            table.put("row2", "col1", 4999)
            val tableString: String = TableTextFormat<String, String, Int?>().apply {
                formatHeaderFun = {
                    when (it) {
                        "O" -> "Other"
                        else -> it
                    }
                }
                rowOrder = String.CASE_INSENSITIVE_ORDER
                colOrder = String.CASE_INSENSITIVE_ORDER
            }.format(table)
            println(tableString)
        }
    }
}