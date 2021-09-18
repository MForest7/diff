import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.E
import kotlin.math.exp
import kotlin.test.*

internal class Test1 {
    private val standardOut = System.out
    private val standardIn = System.`in`
    private val stream = ByteArrayOutputStream()

    private fun checkDP(exp: Array<Array<DPLink>>, fnd: Array<Array<DPLink>>) {
        assert(exp.size == fnd.size)
        for (i in exp.indices)
            assertContentEquals(exp[i], fnd[i])
    }

    private fun checkDiffLines(old: Array<String>, new: Array<String>, diffLines: ArrayList<DiffLine>) {
        var changedOld: MutableList<String> = old.toMutableList()
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

    private fun checkMain(old: Array<String>, new: Array<String>, ans: Array<String>) {
        File("src/test/kotlin/old.txt").outputStream().bufferedWriter().use{ out ->
            old.forEach { out.append("$it\n") }
        }
        File("src/test/kotlin/new.txt").outputStream().bufferedWriter().use{ out ->
            new.forEach { out.append("$it\n") }
        }

        main(arrayOf("src/test/kotlin/old.txt", "src/test/kotlin/new.txt", "src/test/kotlin/output.txt"))
        assertContentEquals(ans, readStringsFromFile("src/test/kotlin/output.txt"))
    }

    @BeforeTest
    fun setUp() {
        System.setOut(PrintStream(stream))
    }

    @AfterTest
    fun tearDown() {
        System.setOut(standardOut)
        System.setIn(standardIn)
    }

    @Test
    fun testDP() {
        checkDP(
            lcsDP(
                arrayOf("a", "a", "b", "a"),
                arrayOf("a", "b", "a", "b")
            ),
            arrayOf(
                arrayOf(DPLink(From.Undefined, 0), DPLink(From.New, 0), DPLink(From.New, 0), DPLink(From.New, 0), DPLink(From.New, 0)),
                arrayOf(DPLink(From.Old, 0), DPLink(From.Common, 1), DPLink(From.New, 1), DPLink(From.Common, 1), DPLink(From.New, 1)),
                arrayOf(DPLink(From.Old, 0), DPLink(From.Common, 1), DPLink(From.New, 1), DPLink(From.Common, 2), DPLink(From.New, 2)),
                arrayOf(DPLink(From.Old, 0), DPLink(From.Old, 1), DPLink(From.Common, 2), DPLink(From.New, 2), DPLink(From.Common, 3)),
                arrayOf(DPLink(From.Old, 0), DPLink(From.Common, 1), DPLink(From.Old, 2), DPLink(From.Common, 3), DPLink(From.New, 3))
            ))

        checkDP(
            lcsDP(
                arrayOf("a","c","c"),
                arrayOf("a","b","d","c")
            ),
            arrayOf(
                arrayOf(DPLink(From.Undefined, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.New, 0)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.Common, 1),DPLink(From.New, 1),DPLink(From.New, 1),DPLink(From.New, 1)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.Old, 1),DPLink(From.New, 1),DPLink(From.New, 1),DPLink(From.Common, 2)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.Old, 1),DPLink(From.New, 1),DPLink(From.New, 1),DPLink(From.Common, 2))
            ))

        checkDP(
            lcsDP(
                arrayOf("a","c","a","c","b","a","a","b"),
                arrayOf("a")
            ),
            arrayOf(
                arrayOf(DPLink(From.Undefined, 0),DPLink(From.New, 0)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.Common, 1)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.Old, 1)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.Common, 1)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.Old, 1)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.Old, 1)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.Common, 1)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.Common, 1)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.Old, 1))
            ))

        checkDP(
            lcsDP(
                arrayOf("c"),
                arrayOf("b","c","b","a","c","a")
            ),
            arrayOf(
                arrayOf(DPLink(From.Undefined, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.New, 0)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.New, 0),DPLink(From.Common, 1),DPLink(From.New, 1),DPLink(From.New, 1),DPLink(From.Common, 1),DPLink(From.New, 1))
            ))

        checkDP(
            lcsDP(
                arrayOf("a","a","a"),
                arrayOf("a","a","a")
            ),
            arrayOf(
                arrayOf(DPLink(From.Undefined, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.New, 0)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.Common, 1),DPLink(From.Common, 1),DPLink(From.Common, 1)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.Common, 1),DPLink(From.Common, 2),DPLink(From.Common, 2)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.Common, 1),DPLink(From.Common, 2),DPLink(From.Common, 3))
            ))


        checkDP(
            lcsDP(
                arrayOf("ab", "bc", "abc", "ba", "bc"),
                arrayOf("bc", "ac", "acb", "bc")
            ),
            arrayOf(
                arrayOf(DPLink(From.Undefined, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.New, 0)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.New, 0)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.Common, 1),DPLink(From.New, 1),DPLink(From.New, 1),DPLink(From.Common, 1)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.Old, 1),DPLink(From.New, 1),DPLink(From.New, 1),DPLink(From.New, 1)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.Old, 1),DPLink(From.New, 1),DPLink(From.New, 1),DPLink(From.New, 1)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.Common, 1),DPLink(From.New, 1),DPLink(From.New, 1),DPLink(From.Common, 2))
            ))

        checkDP(
            lcsDP(
                arrayOf("&^#", " _ ", "&", "%№ё", "300$", "", "День недели - суббота"),
                arrayOf("&", "300$", "&^#", "День недели - суббота")
            ),
            arrayOf(
                arrayOf(DPLink(From.Undefined, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.New, 0)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.Common, 1),DPLink(From.New, 1)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.Old, 1),DPLink(From.New, 1)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.Old, 1),DPLink(From.New, 1)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.Old, 1),DPLink(From.New, 1)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.New, 0),DPLink(From.Common, 1),DPLink(From.New, 1),DPLink(From.New, 1)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.New, 0),DPLink(From.Old, 1),DPLink(From.New, 1),DPLink(From.New, 1)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.New, 0),DPLink(From.Old, 1),DPLink(From.New, 1),DPLink(From.Common, 2))
            ))
    }

    @Test
    fun testDiffLines() {
        var old = arrayOf("a", "a", "b", "a")
        var new = arrayOf("a", "b", "a", "b")
        var lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLinesArray(old, new, lineFrom))

        old =arrayOf("a","c","c")
        new = arrayOf("a","b","d","c")
        lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLinesArray(old, new, lineFrom))

        old = arrayOf("a","c","a","c","b","a","a","b")
        new = arrayOf("a")
        lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLinesArray(old, new, lineFrom))

        old = arrayOf("c")
        new = arrayOf("b","c","b","a","c","a")
        lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLinesArray(old, new, lineFrom))

        old = arrayOf("a","a","a")
        new = arrayOf("a","a","a")
        lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLinesArray(old, new, lineFrom))

        old = arrayOf("ab", "bc", "abc", "ba", "bc")
        new = arrayOf("bc", "ac", "acb", "bc")
        lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLinesArray(old, new, lineFrom))

        old = arrayOf("&^#", " _ ", "&", "%№ё", "300$", "", "День недели - суббота")
        new = arrayOf("&", "300$", "&^#", "День недели - суббота")
        lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLinesArray(old, new, lineFrom))
    }

    @Test
    fun testCompressLines() {
        var old = arrayOf("a", "a", "b", "a")
        var new = arrayOf("a", "b", "a", "b")
        var lineFrom = lcsDP(old, new)
        var diffLines = buildDiffLinesArray(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))

        old =arrayOf("a","c","c")
        new = arrayOf("a","b","d","c")
        lineFrom = lcsDP(old, new)
        diffLines = buildDiffLinesArray(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))

        old = arrayOf("a","c","a","c","b","a","a","b")
        new = arrayOf("a")
        lineFrom = lcsDP(old, new)
        diffLines = buildDiffLinesArray(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))

        old = arrayOf("c")
        new = arrayOf("b","c","b","a","c","a")
        lineFrom = lcsDP(old, new)
        diffLines = buildDiffLinesArray(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))

        old = arrayOf("a","a","a")
        new = arrayOf("a","a","a")
        lineFrom = lcsDP(old, new)
        diffLines = buildDiffLinesArray(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))

        old = arrayOf("ab", "bc", "abc", "ba", "bc")
        new = arrayOf("bc", "ac", "acb", "bc")
        lineFrom = lcsDP(old, new)
        diffLines = buildDiffLinesArray(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))

        old = arrayOf("&^#", " _ ", "&", "%№ё", "300$", "", "День недели - суббота")
        new = arrayOf("&", "300$", "&^#", "День недели - суббота")
        lineFrom = lcsDP(old, new)
        diffLines = buildDiffLinesArray(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))
    }

    @Test
    fun testProject() {
        var old = arrayOf("a", "a", "b", "a")
        var new = arrayOf("a", "b", "a", "b")
        var ans = arrayOf(
            "1del1",
            "< a",
            "5add4",
            "> b"
        )

        checkMain(old, new, ans)
    }
}
