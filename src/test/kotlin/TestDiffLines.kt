import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.E
import kotlin.math.exp
import kotlin.test.*

internal class TestDiffLines {
    @Test
    fun testDiffLinesOnlyLetters() {
        val old = readStringsFromFile("testData/onlyLetters/old.txt")
        val new = readStringsFromFile("testData/onlyLetters/new.txt")
        val lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLines(old, new, lineFrom))
    }

    @Test
    fun testDiffLinesDifferentLength() {
        val old = readStringsFromFile("testData/differentLength/old.txt")
        val new = readStringsFromFile("testData/differentLength/new.txt")
        val lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLines(old, new, lineFrom))
    }

    @Test
    fun testDiffLinesOnlyDel() {
        val old = readStringsFromFile("testData/onlyDel/old.txt")
        val new = readStringsFromFile("testData/onlyDel/new.txt")
        val lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLines(old, new, lineFrom))
    }

    @Test
    fun testDiffLinesOnlyAdd() {
        val old = readStringsFromFile("testData/onlyAdd/old.txt")
        val new = readStringsFromFile("testData/onlyAdd/new.txt")
        val lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLines(old, new, lineFrom))
    }

    @Test
    fun testDiffLinesEqualStrings() {
        val old = readStringsFromFile("testData/equalStrings/old.txt")
        val new = readStringsFromFile("testData/equalStrings/new.txt")
        val lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLines(old, new, lineFrom))
    }

    @Test
    fun testDiffLinesWords() {
        val old = readStringsFromFile("testData/words/old.txt")
        val new = readStringsFromFile("testData/words/new.txt")
        val lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLines(old, new, lineFrom))
    }

    @Test
    fun testDiffLinesRandomCharacters() {
        val old = readStringsFromFile("testData/randomCharacters/old.txt")
        val new = readStringsFromFile("testData/randomCharacters/new.txt")
        val lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLines(old, new, lineFrom))
    }

    private fun checkDiffLines(old: Array<String>, new: Array<String>, diffLines: ArrayList<DiffLine>) {
        val changedOld: MutableList<String> = old.toMutableList()
        var indexOfCurrentLine = 0
        for (line in diffLines) {
            when (line.file) {
                From.Old -> {
                    assert(line.content == changedOld[indexOfCurrentLine])
                    assert(line.content == old[line.index])
                    changedOld.removeAt(indexOfCurrentLine)
                }
                From.New -> {
                    assert(line.content == new[line.index])
                    changedOld.add(indexOfCurrentLine, line.content)
                    indexOfCurrentLine++
                }
                From.Common -> {
                    assert(line.content == changedOld[indexOfCurrentLine])
                    assert(line.content == old[line.index])
                    indexOfCurrentLine++
                }
                else -> assert(false)
            }
        }

        assertContentEquals(new, changedOld.toTypedArray())
    }
}
