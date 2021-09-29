import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.E
import kotlin.math.exp
import kotlin.test.*

internal class TestDP {
    @Test
    fun testDPOnlyLetters() {
        checkDP(
            lcsDP(
                readStringsFromFile("testData/onlyLetters/old.txt"),
                readStringsFromFile("testData/onlyLetters/new.txt")
            ),
            readDP("testData/onlyLetters/dp.txt")
        )
    }

    @Test
    fun testDPDifferentLength() {
        checkDP(
            lcsDP(
                readStringsFromFile("testData/differentLength/old.txt"),
                readStringsFromFile("testData/differentLength/new.txt")
            ),
            readDP("testData/differentLength/dp.txt")
        )
    }

    @Test
    fun testDPOnlyDel() {
        checkDP(
            lcsDP(
                readStringsFromFile("testData/onlyDel/old.txt"),
                readStringsFromFile("testData/onlyDel/new.txt")
            ),
            readDP("testData/onlyDel/dp.txt")
        )
    }

    @Test
    fun testDPOnlyAdd() {
        checkDP(
            lcsDP(
                readStringsFromFile("testData/onlyAdd/old.txt"),
                readStringsFromFile("testData/onlyAdd/new.txt")
            ),
            readDP("testData/onlyAdd/dp.txt")
        )
    }

    @Test
    fun testDPEqualStrings() {
        checkDP(
            lcsDP(
                readStringsFromFile("testData/equalStrings/old.txt"),
                readStringsFromFile("testData/equalStrings/new.txt")
            ),
            readDP("testData/equalStrings/dp.txt")
        )
    }

    @Test
    fun testDPWords() {
        checkDP(
            lcsDP(
                readStringsFromFile("testData/words/old.txt"),
                readStringsFromFile("testData/words/new.txt")
            ),
            readDP("testData/words/dp.txt")
        )
    }

    @Test
    fun testDPRandomCharacters() {
        checkDP(
            lcsDP(
                readStringsFromFile("testData/randomCharacters/old.txt"),
                readStringsFromFile("testData/randomCharacters/new.txt")
            ),
            readDP("testData/randomCharacters/dp.txt")
        )
    }

    private fun checkDP(fnd: Array<Array<DPLink>>, exp: Array<Array<DPLink>>) {
        assert(exp.size == fnd.size)
        for (i in exp.indices)
            assertContentEquals(exp[i], fnd[i])
    }

    private fun readDP(filename: String): Array<Array<DPLink>> {
        val lines = readStringsFromFile(filename)
        val data = List<List<String>>(lines.size) { lines[it].split(" ").dropLast(1) }
        return Array<Array<DPLink>> (data.size / 2) { row ->
            Array<DPLink>(data[row].size) { column ->
                DPLink(
                    when (data[2 * row][column]) {
                        "Old" -> From.Old
                        "New" -> From.New
                        "Common" -> From.Common
                        else -> From.Undefined
                    },
                    data[2 * row + 1][column].toInt()
                )
            }
        }
    }
}
