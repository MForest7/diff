import java.io.File
import java.nio.charset.Charset
import kotlin.collections.ArrayList

/*
    readStringsFromFile возвращает массив, содержащий все строки файла fileName
 */
fun readStringsFromFile(fileName: String): Array<String> = File(fileName).readLines(Charset.defaultCharset()).toTypedArray()

/*
    From - класс, значения которого соответствуют возможным файлам, содержащим принятые на вход строки
 */
enum class From {
    Old, New, Common, Undefined
}

/*
    DPLink - класс, содержащий информацию о текущем состоянии ДП:
        file - файл, содержащий последнюю строку (определяет последний переход для восстановления LCS
        length - длина LCS
 */
data class DPLink(val file: From, val length: Int)

/*
    DiffLine - класс, содержащий для строки:
        file - файл, в котором она находилась
        content - содержание строки
        index - номер в соответствующем файле (если file = From.Common, номер в оригинальном файле)
 */
data class DiffLine (val file: From, val content: String, val index: Int)

/*
    DiffLineBlock содержит подряд идущие DiffLine, сгруппированные по полю file
 */
data class DiffLineBlock (val file: From, val lines: ArrayList<String>, var firstIndex: Int, var lastIndex: Int)

/*
    lcsDP - реализация алгоритма поиска наибольшей общей подпослеовательности
    https://en.wikipedia.org/wiki/Longest_common_subsequence_problem
    lcsDP возвращает матрицу DPLink, содержащих информацию для восстановления lcs
 */
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

/*
    buildDiffLines восстанавливвает порядок изменений с помощью подъёма по матрице предков DPLink
    Результат записывается в возвращаемый ArrayList<DiffLine>
 */
fun buildDiffLines(old: Array<String>, new: Array<String>, lineFrom: Array<Array<DPLink>>): ArrayList<DiffLine> {
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

/*
    compressLines группирует изменения одного типа (добавление, удаление) в DiffLineBlock
 */
fun compressLines(linesArray: ArrayList<DiffLine>): ArrayList<DiffLineBlock> {
    val diffBlocks: ArrayList<DiffLineBlock> = ArrayList()
    var lastLineFrom = From.Undefined
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

/*
    printDiffBlocksToFile записывает в файл вывод утилиты
 */
fun printDiffBlocksToFile(diffArray: ArrayList<DiffLineBlock>, fileName: String) {

    File(fileName).outputStream().bufferedWriter().use { out ->
        diffArray.forEach { block ->
            when (block.file) {
                From.Old -> {
                    when (block.firstIndex) {
                        block.lastIndex -> out.write("del ${block.firstIndex + 1}\n")
                        else -> out.write("del ${block.firstIndex + 1}-${block.lastIndex + 1}\n")
                    }
                    block.lines.forEach { out.write("< $it\n") }
                }
                From.New -> {
                    when (block.firstIndex) {
                        block.lastIndex -> out.write("add ${block.firstIndex + 1}\n")
                        else -> out.write("add ${block.firstIndex + 1}-${block.lastIndex + 1}\n")
                    }
                    block.lines.forEach { out.write("> $it\n") }
                }
                From.Undefined -> assert(false)
            }
        }
    }
}

/*
    printDiffBlocks записывает вывод утилиты в stdout
 */
fun printDiffBlocks(diffArray: ArrayList<DiffLineBlock>) {
    diffArray.forEach { block ->
        when (block.file) {
            From.Old -> {
                when (block.firstIndex) {
                    block.lastIndex -> print("del ${block.firstIndex + 1}\n")
                    else -> print("del ${block.firstIndex + 1}-${block.lastIndex + 1}\n")
                }
                block.lines.forEach { print("\u001B[31m< $it\n\u001B[0m") }
            }
            From.New -> {
                when (block.firstIndex) {
                    block.lastIndex -> print("add ${block.firstIndex + 1}\n")
                    else -> print("add ${block.firstIndex + 1}-${block.lastIndex + 1}\n")
                }
                block.lines.forEach { print("\u001B[32m> $it\n\u001B[0m") }
            }
            From.Undefined -> assert(false)
        }
    }
}

/*
    buildDiffBlocks формирует посзаданным файлам вывод утилиты
 */
fun buildDiffBlocks(oldContent: Array<String>, newContent: Array<String>): ArrayList<DiffLineBlock> {
    val optFrom = lcsDP(oldContent, newContent)
    val diffLines = buildDiffLines(oldContent, newContent, optFrom)
    return compressLines(diffLines)
}

fun main(args: Array<String>) {
    if (args.size < 2) {
        print("""Неверный формат
            | Верно ./pf-2021-diff <file_original> <file_new> <file_diff> 
            | или /pf-2021-diff <file_original> <file_new>\n""".trimMargin())
        assert(false)
    }

    val oldContent = readStringsFromFile(args[0])
    val newContent = readStringsFromFile(args[1])

    val diffBlocks = buildDiffBlocks(oldContent, newContent)

    if (2 in args.indices)
        printDiffBlocksToFile(diffBlocks, args[2])
    else
        printDiffBlocks(diffBlocks)
}
