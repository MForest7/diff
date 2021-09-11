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

fun lcsDP(old: Array<String>, new: Array<String>): Array<Array<String>> {
    val equals = buildEqualsMatrix(old, new)

    val lcs: Array<Array<Int>> = Array(old.size + 1) { Array(new.size + 1) {0} }
    val lineFrom: Array<Array<String>> = Array(old.size + 1) { Array(new.size + 1) {""} }

    for (i in old.indices)
        lineFrom[i][0] = "old"
    for (j in new.indices)
        lineFrom[0][j] = "new"

    for (i in old.indices) {
        for (j in new.indices) {
            if (lcs[i + 1][j + 1] <= lcs[i][j + 1]) {
                lcs[i + 1][j + 1] = lcs[i][j + 1]
                lineFrom[i + 1][j + 1] = "old"
            }
            if (lcs[i + 1][j + 1] <= lcs[i + 1][j]) {
                lcs[i + 1][j + 1] = lcs[i + 1][j]
                lineFrom[i + 1][j + 1] = "new"
            }
            if (equals[i][j] and (lcs[i + 1][j + 1] < lcs[i][j] + 1)) {
                lcs[i + 1][j + 1] = lcs[i][j] + 1
                lineFrom[i + 1][j + 1] = "common"
            }
        }
    }

    return lineFrom
}

fun buildDiffArray(old: Array<String>, new: Array<String>, lineFrom: Array<Array<String>>): ArrayList<Pair<String, String>> {
    var iOld = old.size
    var iNew = new.size

    val diffArray: ArrayList<Pair<String, String>> = arrayListOf()

    while ((iOld > 0) && (iNew > 0)) {
        when (lineFrom[iOld][iNew]) {
            "old" -> {
                iOld--
                diffArray.add(Pair("<", old[iOld]))
            }
            "new" -> {
                iNew--
                diffArray.add(Pair(">", new[iNew]))
            }
            "common" -> {
                iOld--
                iNew--
                diffArray.add(Pair("=", old[iOld]))
            }
            else -> assert(false)
        }
    }

    diffArray.reverse()

    return diffArray
}

fun printDiffArray(diffArray: ArrayList<Pair<String, String>>) {
    for ((flag, line) in diffArray)
        println("$flag $line")
}

fun main(args: Array<String>) {
    val oldContent = readStringsFromFile(readLine()!!)
    val newContent = readStringsFromFile(readLine()!!)

    printDiffArray(buildDiffArray(oldContent, newContent, lcsDP(oldContent, newContent)))
}
