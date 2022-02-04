package afm.common

import afm.common.utils.classLoader
import java.io.ByteArrayOutputStream
import java.io.IOException

// 15 facts at a time - 15 facts in a file
// http://randomfactgenerator.net/
object Facts {

    private const val DEFAULT_FACT = "A Levels are less stressful than the IB Diploma :)"

    private fun getFileAsString(path: String): String {
        classLoader.getResourceAsStream(path).use { inst ->
            ByteArrayOutputStream().use { result ->
                val buffer = ByteArray(1024)
                var length: Int

                while (inst!!.read(buffer).also { length = it } != -1) {
                    result.write(buffer, 0, length)
                }

                return result.toString(Charsets.UTF_8)
            }
        }
    }

    /* Read facts from fileN.txt into factMap
     * 	where N = random number generated between 1 and 5
     */
    private val facts: Array<String> = try {
        val fileNum = (1..5).random()
        val path = "facts/facts$fileNum.txt"

        val str: String = getFileAsString(path)
        val lines = str.lines().take(15)

        lines.toTypedArray()

    } catch (e: Exception) {
        when (e) {
            is NullPointerException, is IOException -> {
                arrayOf(DEFAULT_FACT)
            }
            else -> throw e
        }
    }

    @JvmStatic
    fun getRandomFact(): Pair<Int, String> {
        val id = facts.indices.random()
        val fact = facts.getOrElse(id) { DEFAULT_FACT }

        return (id + 1) to fact
    }
}
