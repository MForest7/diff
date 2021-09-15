import java.io.File
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList

fun readStringsFromFile(fileName: String): Array<String> = File(fileName).readLines(Charset.defaultCharset()).toTypedArray()

fun printFile(fileName: String = readLine()!!) {
    val content = readStringsFromFile(fileName)
    for (line in content) {
        println(line)
    }
}

fun buildEqualsMatrix(old: Array<String>, new: Array<String>): Array<Array<Boolean>> {
    val equalsMatrix: Array<Array<Boolean>> = Array(old.size) { Array(new.size) {false} }
    for (i in old.indices) {
        for (j in new.indices)
            equalsMatrix[i][j] = (old[i] == new[j])
    }
    return equalsMatrix
}

enum class From {
    Old, New, Common, Undefined
}

data class DiffLine (val file: From, val content: String, val index: Int)

fun lcsDP(old: Array<String>, new: Array<String>): Array<Array<From>> {
    val equals = buildEqualsMatrix(old, new)

    val lcs: Array<Array<Int>> = Array(old.size + 1) { Array(new.size + 1) {0} }
    val lineFrom: Array<Array<From>> = Array(old.size + 1) { Array(new.size + 1) {From.Undefined} }

    for (i in old.indices)
        lineFrom[i][0] = From.Old
    for (j in new.indices)
        lineFrom[0][j] = From.New

    for (i in old.indices) {
        for (j in new.indices) {
            if (lcs[i + 1][j + 1] <= lcs[i][j + 1]) {
                lcs[i + 1][j + 1] = lcs[i][j + 1]
                lineFrom[i + 1][j + 1] = From.Old
            }
            if (lcs[i + 1][j + 1] <= lcs[i + 1][j]) {
                lcs[i + 1][j + 1] = lcs[i + 1][j]
                lineFrom[i + 1][j + 1] = From.New
            }
            if (equals[i][j] and (lcs[i + 1][j + 1] < lcs[i][j] + 1)) {
                lcs[i + 1][j + 1] = lcs[i][j] + 1
                lineFrom[i + 1][j + 1] = From.Common
            }
        }
    }

    return lineFrom
}

fun buildDiffLinesArray(old: Array<String>, new: Array<String>, lineFrom: Array<Array<From>>): ArrayList<DiffLine> {
    var iOld = old.size
    var iNew = new.size

    val diffLinesArray: ArrayList<DiffLine> = arrayListOf()

    while ((iOld > 0) || (iNew > 0)) {
        when (lineFrom[iOld][iNew]) {
            From.Old -> {
                iOld--
                diffLinesArray.add(DiffLine(From.Old, old[iOld], iOld))
            }
            From.New -> {
                iNew--
                diffLinesArray.add(DiffLine(From.New, new[iNew], iNew))
            }
            From.Common -> {
                iOld--
                iNew--
                diffLinesArray.add(DiffLine(From.Common, old[iOld], iOld))
            }
            else -> assert(false)
        }
    }

    diffLinesArray.reverse()

    return diffLinesArray
}

data class DiffLineBlock (val file: From, val lines: ArrayList<String>, var firstIndex: Int, var lastIndex: Int)

fun compressLines(linesArray: ArrayList<DiffLine>): ArrayList<DiffLineBlock> {
    val diffBlocks: ArrayList<DiffLineBlock> = ArrayList()
    var lastLineFrom = From.Undefined
    var addedFromOld = 0
    var addedFromNew = 0
    for (line in linesArray) {
        if (line.file == lastLineFrom) {
            diffBlocks.last().lines.add(line.content)
            diffBlocks.last().lastIndex = line.index
        } else {
            diffBlocks.add(DiffLineBlock(line.file, arrayListOf(line.content), line.index, line.index))
        }
        lastLineFrom = line.file
    }
    return diffBlocks
}

fun printDiffLines(diffArray: ArrayList<DiffLine>) {
    for ((flag, line) in diffArray)
        println("${when (flag) {
            From.Old -> "< "
            From.New -> "> "
            From.Common -> "= "
            From.Undefined -> "UNKNOWN "
        }        } $line")
}

fun printDiffArray(diffArray: ArrayList<DiffLineBlock>) {
    for (block in diffArray) {
        when (block.file) {
            From.Old -> println("del${block.firstIndex}-${block.lastIndex}")
            From.New -> println("add${block.firstIndex}-${block.lastIndex}")
            else -> continue
        }
        for (line in block.lines) {
            println("${when (block.file) {
                From.Old -> "< "
                From.New -> "> "
                From.Common -> "= "
                From.Undefined -> "UNKNOWN "
            }        } $line")
        }
    }
}

fun main(args: Array<String>) {
    val oldContent = readStringsFromFile(readLine()!!)
    val newContent = readStringsFromFile(readLine()!!)

    val optFrom = lcsDP(oldContent, newContent)
    val diffLinesArray = buildDiffLinesArray(oldContent, newContent, optFrom)
    printDiffLines(diffLinesArray)
    val changedBlocks = compressLines(diffLinesArray)
    printDiffArray(changedBlocks)
}
