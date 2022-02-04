@file:JvmName("CoreUtils")
@file:JvmMultifileClass

package afm.common.utils

// this 500ns vs regex 2000ns
fun String.splitByCapitals(): Array<String> {
    val res = mutableListOf<String>()

    var start = 0

    for (i in 1 until length) {
        val here = this[i]
        if (here.isUpperCase()) {
            res.add(substring(start, i))
            start = i
        }
    }

    // add remaining
    res.add(substring(start))

    res.removeIf(String?::isNullOrBlank)

    return res.toTypedArray()
}

fun String.remove(s: String, ignoreCase: Boolean = false) = replace(s, "", ignoreCase)

fun String.remove(c: Char, ignoreCase: Boolean = false) = replace(c.toString(), "", ignoreCase)

fun String.splitIgnoreEmpty(
    vararg delimiters: String,
    ignoreCase: Boolean = false,
    limit: Int = 0,
): List<String> =
    split(*delimiters, ignoreCase = ignoreCase, limit = limit)
        .filter { it.isNotEmpty() }
