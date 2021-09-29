import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.E
import kotlin.math.exp
import kotlin.test.*

internal class TestCompressLines {
    @Test
    fun testCompressLinesOnlyLetters() {
        val old = readStringsFromFile("testData/onlyLetters/old.txt")
        val new = readStringsFromFile("testData/onlyLetters/new.txt")
        val lineFrom = lcsDP(old, new)
        val diffLines = buildDiffLines(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))
    }

    @Test
    fun testCompressLinesDifferentLength() {
        val old = readStringsFromFile("testData/differentLength/old.txt")
        val new = readStringsFromFile("testData/differentLength/new.txt")
        val lineFrom = lcsDP(old, new)
        val diffLines = buildDiffLines(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))
    }

    @Test
    fun testCompressLinesOnlyDel() {
        val old = readStringsFromFile("testData/onlyDel/old.txt")
        val new = readStringsFromFile("testData/onlyDel/new.txt")
        val lineFrom = lcsDP(old, new)
        val diffLines = buildDiffLines(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))
    }

    @Test
    fun testCompressLinesOnlyAdd() {
        val old = readStringsFromFile("testData/onlyAdd/old.txt")
        val new = readStringsFromFile("testData/onlyAdd/new.txt")
        val lineFrom = lcsDP(old, new)
        val diffLines = buildDiffLines(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))
    }

    @Test
    fun testCompressLinesEqualStrings() {
        val old = readStringsFromFile("testData/equalStrings/old.txt")
        val new = readStringsFromFile("testData/equalStrings/new.txt")
        val lineFrom = lcsDP(old, new)
        val diffLines = buildDiffLines(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))
    }

    @Test
    fun testCompressLinesWords() {
        val old = readStringsFromFile("testData/words/old.txt")
        val new = readStringsFromFile("testData/words/new.txt")
        val lineFrom = lcsDP(old, new)
        val diffLines = buildDiffLines(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))
    }

    @Test
    fun testCompressLinesRandomSymbols() {
        val old = readStringsFromFile("testData/randomCharacters/old.txt")
        val new = readStringsFromFile("testData/randomCharacters/new.txt")
        val lineFrom = lcsDP(old, new)
        val diffLines = buildDiffLines(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))
    }

    private fun checkCompressLines(diffLines: ArrayList<DiffLine>, blocks: ArrayList<DiffLineBlock>) {
        var indexOfBlock = -1
        var indexInBlock = 0
        var currentBlockFrom = From.Undefined
        for (line in diffLines) {
            when (line.file) {
                currentBlockFrom -> {
                    assert(line.content == blocks[indexOfBlock].lines[indexInBlock])
                    indexInBlock++
                }
                else -> {
                    if (indexOfBlock != -1)
                        assert(indexInBlock == blocks[indexOfBlock].lines.size)
                    assert(!((line.file == From.Old) and (currentBlockFrom == From.New)))
                    indexInBlock = 1
                    indexOfBlock += 1
                    currentBlockFrom = blocks[indexOfBlock].file
                    assert(line.file == blocks[indexOfBlock].file)
                    assert(line.content == blocks[indexOfBlock].lines.first())
                }
            }
        }
        assert(indexOfBlock == blocks.lastIndex)
        assert(indexInBlock == blocks.last().lines.size)
    }
}
