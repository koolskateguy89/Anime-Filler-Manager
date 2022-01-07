package afm.common

import java.io.ByteArrayOutputStream
import java.io.IOException


// 15 facts at a time - 15 facts in a file
// http://randomfactgenerator.net/

private val factMap = mutableMapOf<Int, String>()

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

        val str: String = getFileAsString(path)
        val lines = str.lines().take(15)

        lines.forEachIndexed { i, fact -> factMap[i + 1] = fact }

    } catch (e: Exception) {
        when (e) {
            is NullPointerException, is IOException -> factMap[factMap.size + 1] = DEFAULT_FACT
            else -> throw e
        }
    }
}

private fun getFileAsString(path: String): String {
    classLoader.getResourceAsStream(path).use { `in` ->
        ByteArrayOutputStream().use { result ->
            val buffer = ByteArray(1024)
            var length: Int

            while (`in`.read(buffer).also { length = it } != -1) {
                result.write(buffer, 0, length)
            }

            return result.toString(Charsets.UTF_8)
        }
    }
}

fun getRandomFact(): Pair<Int, String> {
    val id = (1..factMap.size).random()
    val fact = factMap[id] ?: DEFAULT_FACT

    return id to fact
}
