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
        lcs[i + 1][0] = DPLink(From.Old, 0)
    for (j in new.indices)
        lcs[0][j + 1] = DPLink(From.New, 0)

    for (i in old.indices) {
        for (j in new.indices) {
            if (lcs[i + 1][j + 1].length <= lcs[i][j + 1].length) {
                lcs[i + 1][j + 1] = DPLink(From.Old, lcs[i][j + 1].length)
            }
            if (lcs[i + 1][j + 1].length <= lcs[i + 1][j].length) {
                lcs[i + 1][j + 1] = DPLink(From.New, lcs[i + 1][j].length)
            }
            if ((old[i] == new[j]) and (lcs[i + 1][j + 1].length <= lcs[i][j].length + 1)) {
                lcs[i + 1][j + 1] = DPLink(From.Common, lcs[i][j].length + 1)
            }
        }
    }

    return lcs
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

fun printDiffBlocksToFile(diffArray: ArrayList<DiffLineBlock>, fileName: String) {
    File(fileName).outputStream().bufferedWriter().use { out ->
        var indexInOld = 1
        var indexInNew = 1
        diffArray.forEach { block ->
            when (block.file) {
                From.Old -> {
                    when (block.firstIndex) {
                        block.lastIndex -> out.write("${block.firstIndex + 1}del${indexInNew}\n")
                        else -> out.write("${block.firstIndex + 1}-${block.lastIndex + 1}del${indexInNew}\n")
                    }
                    block.lines.forEach { out.write("< $it\n") }
                    indexInOld += block.lines.size
                }
                From.New -> {
                    when (block.firstIndex) {
                        block.lastIndex -> out.write("${indexInOld}add${block.firstIndex + 1}\n")
                        else -> out.write("${indexInOld}add${block.firstIndex + 1}-${block.lastIndex + 1}\n")
                    }
                    block.lines.forEach { out.write("> $it\n") }
                    indexInNew += block.lines.size
                }
                From.Common -> {
                    indexInOld += block.lines.size
                    indexInNew += block.lines.size
                }
                From.Undefined -> assert(false)
            }
        }
    }
}

fun printDiffBlocks(diffArray: ArrayList<DiffLineBlock>) {
    var indexInOld = 1
    var indexInNew = 1
    for (block in diffArray) {
        when (block.file) {
            From.Old -> {
                when (block.firstIndex) {
                    block.lastIndex -> print("${block.firstIndex + 1}del${indexInNew}\n")
                    else -> print("${block.firstIndex + 1}-${block.lastIndex + 1}del${indexInNew}\n")
                }
                block.lines.forEach { print("< $it\n") }
                indexInOld += block.lines.size
            }
            From.New -> {
                when (block.firstIndex) {
                    block.lastIndex -> print("${indexInOld}add${block.firstIndex + 1}\n")
                    else -> print("${indexInOld}add${block.firstIndex + 1}-${block.lastIndex + 1}\n")
                }
                block.lines.forEach { print("> $it\n") }
                indexInNew += block.lines.size
            }
            From.Common -> {
                indexInOld += block.lines.size
                indexInNew += block.lines.size
            }
            else -> continue
        }
    }
}

fun buildDiffBlocks(oldContent: Array<String>, newContent: Array<String>): ArrayList<DiffLineBlock> {
    val optFrom = lcsDP(oldContent, newContent)
    val diffLinesArray = buildDiffLinesArray(oldContent, newContent, optFrom)
    return compressLines(diffLinesArray)
}

fun main(args: Array<String>) {
    val oldContent = readStringsFromFile(args[0])
    val newContent = readStringsFromFile(args[1])

    val diffBlocks = buildDiffBlocks(oldContent, newContent)

    if (2 in args.indices)
        printDiffBlocksToFile(diffBlocks, args[2])
    else
        printDiffBlocks(diffBlocks)
}
