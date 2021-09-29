import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.E
import kotlin.math.exp
import kotlin.test.*

internal class TestMain {
    private val standardOut = System.out
    private val standardIn = System.`in`
    private val stream = ByteArrayOutputStream()

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
    fun testProjectOnlyLetters() {
        val old = readStringsFromFile("testData/onlyLetters/old.txt")
        val new = readStringsFromFile("testData/onlyLetters/new.txt")
        val ans = readStringsFromFile("testData/onlyLetters/diff.txt")
        checkMain(old, new, ans)
    }


    @Test
    fun testProjectDifferentLength() {
        val old = readStringsFromFile("testData/differentLength/old.txt")
        val new = readStringsFromFile("testData/differentLength/new.txt")
        val ans = readStringsFromFile("testData/differentLength/diff.txt")
        checkMain(old, new, ans)
    }

    @Test
    fun testProjectOnlyDel() {
        val old = readStringsFromFile("testData/onlyDel/old.txt")
        val new = readStringsFromFile("testData/onlyDel/new.txt")
        val ans = readStringsFromFile("testData/onlyDel/diff.txt")
        checkMain(old, new, ans)
    }

    @Test
    fun testProjectOnlyAdd() {
        val old = readStringsFromFile("testData/onlyAdd/old.txt")
        val new = readStringsFromFile("testData/onlyAdd/new.txt")
        val ans = readStringsFromFile("testData/onlyAdd/diff.txt")
        checkMain(old, new, ans)
    }

    @Test
    fun testProjectEqualStrings() {
        val old = readStringsFromFile("testData/equalStrings/old.txt")
        val new = readStringsFromFile("testData/equalStrings/new.txt")
        val ans = readStringsFromFile("testData/equalStrings/diff.txt")
        checkMain(old, new, ans)
        checkMain(old, new, ans)
    }

    @Test
    fun testProjectWords() {
        val old = readStringsFromFile("testData/words/old.txt")
        val new = readStringsFromFile("testData/words/new.txt")
        val ans = readStringsFromFile("testData/words/diff.txt")
        checkMain(old, new, ans)
        checkMain(old, new, ans)
    }

    @Test
    fun testProjectRandomCharacters() {
        val old = readStringsFromFile("testData/randomCharacters/old.txt")
        val new = readStringsFromFile("testData/randomCharacters/new.txt")
        val ans = readStringsFromFile("testData/randomCharacters/diff.txt")
        checkMain(old, new, ans)
        checkMain(old, new, ans)
    }

    private fun checkMain(old: Array<String>, new: Array<String>, ans: Array<String>) {
        File("src/test/kotlin/old.txt").outputStream().bufferedWriter().use{ out ->
            old.forEach { out.write("$it\n") }
        }
        File("src/test/kotlin/new.txt").outputStream().bufferedWriter().use{ out ->
            new.forEach { out.write("$it\n") }
        }

        main(arrayOf("src/test/kotlin/old.txt", "src/test/kotlin/new.txt", "src/test/kotlin/output.txt"))
        assertContentEquals(ans, readStringsFromFile("src/test/kotlin/output.txt"))
    }
}
