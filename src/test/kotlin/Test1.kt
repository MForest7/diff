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
    fun testDPOnlyLetters() {
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
    }

    @Test
    fun testDPDifferentLength() {
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
    }

    @Test
    fun testDPOnlyDel() {
        checkDP(
            lcsDP(
                arrayOf("a", "c", "a", "c", "b", "a", "a", "b"),
                arrayOf("a")
            ),
            arrayOf(
                arrayOf(DPLink(From.Undefined, 0), DPLink(From.New, 0)),
                arrayOf(DPLink(From.Old, 0), DPLink(From.Common, 1)),
                arrayOf(DPLink(From.Old, 0), DPLink(From.Old, 1)),
                arrayOf(DPLink(From.Old, 0), DPLink(From.Common, 1)),
                arrayOf(DPLink(From.Old, 0), DPLink(From.Old, 1)),
                arrayOf(DPLink(From.Old, 0), DPLink(From.Old, 1)),
                arrayOf(DPLink(From.Old, 0), DPLink(From.Common, 1)),
                arrayOf(DPLink(From.Old, 0), DPLink(From.Common, 1)),
                arrayOf(DPLink(From.Old, 0), DPLink(From.Old, 1))
            ))
    }

    @Test
    fun testDPOnlyAdd() {
        checkDP(
            lcsDP(
                arrayOf("c"),
                arrayOf("b","c","b","a","c","a")
            ),
            arrayOf(
                arrayOf(DPLink(From.Undefined, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.New, 0),DPLink(From.New, 0)),
                arrayOf(DPLink(From.Old, 0),DPLink(From.New, 0),DPLink(From.Common, 1),DPLink(From.New, 1),DPLink(From.New, 1),DPLink(From.Common, 1),DPLink(From.New, 1))
            ))
    }

    @Test
    fun testDPEqualStrings() {
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
    }

    @Test
    fun testDPWords() {
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
    }

    @Test
    fun testDPRandomCharacters() {
        checkDP(
            lcsDP(
                arrayOf("&^#", " _ ", "k", "%№ё", "300$", "", "День недели - суббота"),
                arrayOf("K", "300$", "&^#", "День недели - суббота")
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
    fun testDiffLinesOnlyLetters() {
        val old = arrayOf("a", "a", "b", "a")
        val new = arrayOf("a", "b", "a", "b")
        val lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLines(old, new, lineFrom))
    }

    @Test
    fun testDiffLinesDifferentLength() {
        val old = arrayOf("a", "c", "c")
        val new = arrayOf("a", "b", "d", "c")
        val lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLines(old, new, lineFrom))
    }

    @Test
    fun testDiffLinesOnlyDel() {
        val old = arrayOf("a", "c", "a", "c", "b", "a", "a", "b")
        val new = arrayOf("a")
        val lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLines(old, new, lineFrom))
    }

    @Test
    fun testDiffLinesOnlyAdd() {
        val old = arrayOf("c")
        val new = arrayOf("b", "c", "b", "a", "c", "a")
        val lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLines(old, new, lineFrom))
    }

    @Test
    fun testDiffLinesEqualStrings() {
        val old = arrayOf("a", "a", "a")
        val new = arrayOf("a", "a", "a")
        val lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLines(old, new, lineFrom))
    }

    @Test
    fun testDiffLinesWords() {
        val old = arrayOf("ab", "bc", "abc", "ba", "bc")
        val new = arrayOf("bc", "ac", "acb", "bc")
        val lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLines(old, new, lineFrom))
    }

    @Test
    fun testDiffLinesRandomCharacters() {
        val old = arrayOf("&^#", " _ ", "k", "%№ё", "300$", "", "День недели - суббота")
        val new = arrayOf("K", "300$", "&^#", "День недели - суббота")
        val lineFrom = lcsDP(old, new)
        checkDiffLines(old, new, buildDiffLines(old, new, lineFrom))
    }

    @Test
    fun testCompressLinesOnlyLetters() {
        val old = arrayOf("a", "a", "b", "a")
        val new = arrayOf("a", "b", "a", "b")
        val lineFrom = lcsDP(old, new)
        val diffLines = buildDiffLines(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))
    }

    @Test
    fun testCompressLinesDifferentLength() {
        val old = arrayOf("a", "c", "c")
        val new = arrayOf("a", "b", "d", "c")
        val lineFrom = lcsDP(old, new)
        val diffLines = buildDiffLines(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))
    }

    @Test
    fun testCompressLinesOnlyDel() {
        val old = arrayOf("a", "c", "a", "c", "b", "a", "a", "b")
        val new = arrayOf("a")
        val lineFrom = lcsDP(old, new)
        val diffLines = buildDiffLines(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))
    }

    @Test
    fun testCompressLinesOnlyAdd() {
        val old = arrayOf("c")
        val new = arrayOf("b", "c", "b", "a", "c", "a")
        val lineFrom = lcsDP(old, new)
        val diffLines = buildDiffLines(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))
    }

    @Test
    fun testCompressLinesEqualStrings() {
        val old = arrayOf("a", "a", "a")
        val new = arrayOf("a", "a", "a")
        val lineFrom = lcsDP(old, new)
        val diffLines = buildDiffLines(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))
    }

    @Test
    fun testCompressLinesWords() {
        val old = arrayOf("ab", "bc", "abc", "ba", "bc")
        val new = arrayOf("bc", "ac", "acb", "bc")
        val lineFrom = lcsDP(old, new)
        val diffLines = buildDiffLines(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))
    }

    @Test
    fun testCompressLinesRandomSymbols() {
        val old = arrayOf("&^#", " _ ", "k", "%№ё", "300$", "", "День недели - суббота")
        val new = arrayOf("K", "300$", "&^#", "День недели - суббота")
        val lineFrom = lcsDP(old, new)
        val diffLines = buildDiffLines(old, new, lineFrom)
        checkCompressLines(diffLines, compressLines(diffLines))
    }

    @Test
    fun testProjectOnlyLetters() {
        val old = arrayOf("a", "a", "b", "a")
        val new = arrayOf("a", "b", "a", "b")
        val ans = arrayOf(
            "del 1",
            "< a",
            "add 4",
            "> b"
        )
        checkMain(old, new, ans)
    }


    @Test
    fun testProjectDifferentLength() {
        val old = arrayOf("a", "c", "c")
        val new = arrayOf("a", "b", "d", "c")
        val ans = arrayOf(
            "del 2",
            "< c",
            "add 2-3",
            "> b",
            "> d"
        )
        checkMain(old, new, ans)
    }

    @Test
    fun testProjectOnlyDel() {
        val old = arrayOf("a", "c", "a", "c", "b", "a", "a", "b")
        val new = arrayOf("a")
        val ans = arrayOf(
            "del 1-6",
            "< a",
            "< c",
            "< a",
            "< c",
            "< b",
            "< a",
            "del 8",
            "< b"
        )
        checkMain(old, new, ans)
    }

    @Test
    fun testProjectOnlyAdd() {
        val old = arrayOf("c")
        val new = arrayOf("b", "c", "b", "a", "c", "a")
        val ans = arrayOf(
            "add 1-4",
            "> b",
            "> c",
            "> b",
            "> a",
            "add 6",
            "> a"
        )
        checkMain(old, new, ans)
    }

    @Test
    fun testProjectEqualStrings() {
        val old = arrayOf("a", "a", "a")
        val new = arrayOf("a", "a", "a")
        val ans = emptyArray<String>()
        checkMain(old, new, ans)
    }

    @Test
    fun testProjectWords() {
        val old = arrayOf("ab", "bc", "abc", "ba", "bc")
        val new = arrayOf("bc", "ac", "acb", "bc")
        val ans = arrayOf(
            "del 1",
            "< ab",
            "del 3-4",
            "< abc",
            "< ba",
            "add 2-3",
            "> ac",
            "> acb"
        )
        checkMain(old, new, ans)
    }

    @Test
    fun testProjectRandomCharacters() {
        val old = arrayOf("&^#", " _ ", "k", "%№ё", "300$", "", "День недели - суббота")
        val new = arrayOf("K", "300$", "&^#", "День недели - суббота")
        val ans = arrayOf(
            "del 1-4",
            "< &^#",
            "<  _ ",
            "< k",
            "< %№ё",
            "add 1",
            "> K",
            "del 6",
            "< ",
            "add 3",
            "> &^#"
        )
        checkMain(old, new, ans)
    }
}
