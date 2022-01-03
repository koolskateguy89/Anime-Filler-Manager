package afm.utils

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets


// 15 facts at a time - 15 facts in a file
// atm only 2 fact files
// http://randomfactgenerator.net/

private val factMap = HashMap<Int, String>()

private const val DEFAULT_FACT = "A Levels are less stressful than the IB Diploma :)"

/* Read text file fileX.txt into factMap
 * 	where X = random number generated between 1 and 5
 *
 * - Called in StartScreen initialiser.
 */
fun init() {
    try {
        val fileNum = (1..5).random()

        val path = "facts/facts$fileNum.txt"

        val file: String = getFileAsString(path)
        val lines = file.lines().subList(0, 15)

        for (i in lines.indices) {
            factMap[i + 1] = lines[i]
        }
    } catch (e: IOException) {
        factMap[factMap.size + 1] = DEFAULT_FACT
        e.printStackTrace()
    }
}

fun getRandomFact(): Pair<Int, String> {
    val id = (1..factMap.size).random()
    val fact = factMap[id] ?: DEFAULT_FACT

    return id to fact
}

private fun getFileAsString(path: String): String {
    classLoader.getResourceAsStream(path).use { `in` ->
        ByteArrayOutputStream().use { result ->
            if (`in` == null)
                return ""

            val buffer = ByteArray(1024)
            var length: Int

            while (`in`.read(buffer).also { length = it } != -1) {
                result.write(buffer, 0, length)
            }
            return result.toString(StandardCharsets.UTF_8)
        }
    }
}
