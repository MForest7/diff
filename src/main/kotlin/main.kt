import java.io.File
import java.nio.charset.Charset
import kotlin.collections.ArrayList

fun readStringsFromFile(fileName: String): Array<String> = File(fileName).readLines(Charset.defaultCharset()).toTypedArray()

fun printFile(fileName: String = readLine()!!) {
    val content = readStringsFromFile(fileName)
    for (line in content) {
        println(line)
    }
}

enum class From {
    Old, New, Common, Undefined
}

data class DPLink(val file: From, val length: Int)

fun lcsDP(old: Array<String>, new: Array<String>): Array<Array<DPLink>> {
    val lcs: Array<Array<DPLink>> = Array(old.size + 1) { Array(new.size + 1) {DPLink(From.Undefined, 0)} }

    for (i in old.indices)
        lcs[i][0] = DPLink(From.Old, 0)
    for (j in new.indices)
        lcs[0][j] = DPLink(From.New, 0)

    for (i in old.indices) {
        for (j in new.indices) {
            if (lcs[i + 1][j + 1].length <= lcs[i][j + 1].length) {
                lcs[i + 1][j + 1] = DPLink(From.Old, lcs[i][j + 1].length)
            }
            if (lcs[i + 1][j + 1].length <= lcs[i + 1][j].length) {
                lcs[i + 1][j + 1] = DPLink(From.New, lcs[i + 1][j].length)
            }
            if ((old[i] == new[j]) and (lcs[i + 1][j + 1].length < lcs[i][j].length + 1)) {
                lcs[i + 1][j + 1] = DPLink(From.Common, lcs[i][j].length + 1)
            }
        }
    }

    return lcs
}

fun lcsDPSpaceEfficient(old: Array<String>, new: Array<String>): IntArray {
    var lcs: IntArray = IntArray(new.size + 1) { 0 }
    for (i in old.indices) {
        val lcsNew = IntArray(new.size + 1) { 0 }
        for (j in new.indices) {
            if (lcsNew[j + 1] <= lcs[j + 1]) {
                lcsNew[j + 1] = lcs[j + 1]
            }
            if (lcsNew[j + 1] <= lcsNew[j]) {
                lcsNew[j + 1] = lcsNew[j]
            }
            if ((old[i] == new[j]) and (lcsNew[j + 1] < lcs[j] + 1)) {
                lcsNew[j + 1] = lcs[j] + 1
            }
        }
        lcs = lcsNew
    }
    return lcs
}

fun reqLCS(old: List<String>, new: List<String>): IntArray {
    when (old.size) {
        0 -> return intArrayOf()
        1 -> return if (new.contains(old.first())) intArrayOf(new.indexOf(old.first())) else intArrayOf()
        else -> {
            val prefLCS = lcsDPSpaceEfficient(
                old.take(old.size / 2).toTypedArray(),
                new.toTypedArray())
            val suffLCS = lcsDPSpaceEfficient(
                old.drop(old.size / 2).toTypedArray().reversedArray(),
                new.reversed().toTypedArray()
            ).reversedArray()

            val merged = IntArray(new.size + 1) { prefLCS[it] + suffLCS[it] }
            val split = merged.indexOf(merged.maxOrNull()?:0)

            val left = reqLCS(old.take(old.size / 2), new.take(split))
            val right = reqLCS(old.drop(old.size / 2), new.drop(split))
            for (i in right.indices) right[i] += split
            return left.plus(right.toList())
        }
    }
}

fun findCommonLinesInNew (old: Array<String>, new: Array<String>): ArrayList<Int> {
    TODO()
}

data class DiffLine (val file: From, val content: String, val index: Int)

fun buildDiffLinesArray(old: Array<String>, new: Array<String>, lineFrom: Array<Array<DPLink>>): ArrayList<DiffLine> {
    var iOld = old.size
    var iNew = new.size

    val diffLinesArray: ArrayList<DiffLine> = arrayListOf()

    while ((iOld > 0) || (iNew > 0)) {
        when (lineFrom[iOld][iNew].file) {
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
    println(reqLCS(oldContent.toList(), newContent.toList()).contentToString())
}
