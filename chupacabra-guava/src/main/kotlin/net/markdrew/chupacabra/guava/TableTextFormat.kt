package net.markdrew.chupacabra.guava

import com.google.common.collect.HashBasedTable
import com.google.common.collect.Table
import kotlin.math.max
import kotlin.math.min

/**
 * Formats a Guava [Table] as text
 */
class TableTextFormat<R : Any, C, V>(
    val columnSeparator: String = " |",
    val maxColumnWidth: Int = Int.MAX_VALUE,
    val maxTableWidth: Int = 200,
    val columnsLabel: String = "",
    val rowsLabel: String = "",
    val minColumnWidth: Int = 2
) {

    var formatValueFun: (V?) -> Any? = { it.toString() }
    var formatHeaderFun: (Any?) -> Any? = { it.toString() }
    var formatRowHeaderFun: ((Any?) -> Any?)? = null
    var formatColHeaderFun: ((Any?) -> Any?)? = null
    var colKeyFun: ((colNum: Int, colLabel: String) -> String)? = null
    var rowOrder: Comparator<in R>? = null
    var colOrder: Comparator<in C>? = null
    
    private var summarizeColLabel: String = ""
    private var summarizeColFun: ((Collection<V?>) -> V)? = null
    fun summarizeCols(label: String, sumFun: (Collection<V?>) -> V) {
        summarizeColLabel = label
        summarizeColFun = sumFun
    }
    
    private fun formatValue(value: V?): String = formatValueFun(value).toString().trimStart()
    private fun formatRowHeader(header: Any?): String =
        (formatRowHeaderFun ?: formatHeaderFun)(header).toString().trimStart()
    private fun formatColHeader(header: Any?): String =
        (formatColHeaderFun ?: formatHeaderFun)(header).toString().trimStart()
    private fun formatColHeader(colNum: Int, colHeader: Any?): String =
        formatColHeader(colHeader).let { colKeyFun?.invoke(colNum, it) ?: it }

    fun format(table: Table<R, C, V?>): String {
        val rowKeys = table.rowKeySet()
        val colKeys = table.columnKeySet()
        val nCols = colKeys.size
        if (nCols == 0) throw Exception("Can't format a table with no columns!")
        
        val labelsLabel = rowsLabel + if (columnsLabel.isBlank()) "" else """ \ $columnsLabel"""
        val rowHeaderWidth: Int = rowKeys
            .map { formatRowHeader(it).length }
            .plus(labelsLabel.length)
            .plus(summarizeColLabel.length)
            .max()

        val colSepWidth = columnSeparator.length
        val maxCol = ((maxTableWidth - rowHeaderWidth - nCols * colSepWidth)/nCols)
            .coerceIn(minColumnWidth, maxColumnWidth)
        
        val orderedColKeys: List<C> = colOrder?.let { colKeys.sortedWith(it) } ?: colKeys.toList()
        val colWidths: Map<C, Int> = orderedColKeys.withIndex().associate { (colNum, colKey) ->
            val colHeaderWidth = min(formatColHeader(colNum, colKey).length, maxCol)
            val maxNaturalWidth: Int = rowKeys.fold(colHeaderWidth) { maxWidth, rowKey ->
                // TODO this is inefficient because it's formatting every cell twice
                max(maxWidth, formatValue(table[rowKey, colKey]).length)
            }
            colKey to maxNaturalWidth
        }

        fun formatRow(formattedRowHeader: String, row: Map<C, V?>): String =
            "%${rowHeaderWidth}s".format(formattedRowHeader) + columnSeparator +
                    colWidths.entries.joinToString(columnSeparator) { (colKey, width) ->
                        "%${width}s".format(formatValue(row[colKey]))
                    }

        fun formatRow(rowKey: R, row: Map<C, V?>): String = formatRow(formatRowHeader(rowKey), row)

        val headerRow: String = colWidths.entries.withIndex().joinToString(
                separator = columnSeparator,
                prefix = "%${rowHeaderWidth}s$columnSeparator".format(labelsLabel)
        ) { (index, entry) ->
            val (colKey, colWidth) = entry
            "%$colWidth.${colWidth}s".format(formatColHeader(index, colKey))
        }

        val summaryRow: String = summarizeColFun?.let { sumFun ->
            val horizontalLine: String = "_".repeat(rowHeaderWidth) +
                    columnSeparator + colWidths.entries.joinToString(columnSeparator) { (_, width) -> "_".repeat(width) }
            "\n" + horizontalLine +
            "\n" + formatRow(summarizeColLabel, table.columnMap().mapValues { (_, map) -> sumFun(map.values) })
        }.orEmpty()

        val orderedRowKeys: List<R> = rowOrder?.let { rowKeys.sortedWith(it) } ?: rowKeys.toList()
        val formattedTableString = headerRow + "\n" + orderedRowKeys.joinToString("\n") { rowKey ->
            formatRow(rowKey, table.row(rowKey))
        } + summaryRow

        return if (colKeyFun == null) {
            formattedTableString
        } else {
            formattedTableString + "\n\n" + buildLegend(orderedColKeys)
        }
    }

    private fun buildLegend(orderedColKeys: List<C>): String = orderedColKeys.withIndex().joinToString(
        separator = "\n  ",
        prefix = "Column Key:\n  "
    ) { (index, colKey) ->
        val headerLabel = formatColHeader(colKey)
        colKeyFun?.invoke(index, headerLabel) + " = " + headerLabel
    }

    companion object {

        fun <R : Any, C, V> format(table: Table<R, C, V?>): String = TableTextFormat<R, C, V?>().format(table)

        fun numberedColKeyFun(startAt: Int = 1): (colNum: Int, colLabel: String) -> String =
            { colNum, _ -> (startAt + colNum).toString() }
        
        fun letteredColKeyFun(startAt: Char = 'a'): (colNum: Int, colLabel: String) -> String =
            { colNum, _ -> (startAt + colNum).toString() }
        
        fun uniqueAbbreviatedColKeyFun(keyLen: Int): (colNum: Int, colLabel: String) -> String {
            val keySet = mutableSetOf<String>()
            val keyMap = mutableMapOf<String, String>()
            return { _, colLabel -> keyMap.computeIfAbsent(colLabel) { 
                    var candidateKey = colLabel.take(keyLen)
                    var mutateIndex = keyLen - 1
                    while (keySet.contains(candidateKey)) {
                        var tryCharAt = mutateIndex + 1
                        while (keySet.contains(candidateKey) && tryCharAt < colLabel.length) {
                            candidateKey = colLabel.take(mutateIndex) + colLabel.drop(tryCharAt).take(keyLen - mutateIndex)
                            tryCharAt++
                        }
                        mutateIndex--
                    }
                    keySet.add(candidateKey)
                    candidateKey
                }
            }
        }
        
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