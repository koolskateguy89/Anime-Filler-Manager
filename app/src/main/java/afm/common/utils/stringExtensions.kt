@file:JvmName("Utils")
@file:JvmMultifileClass

package afm.common.utils

import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent

fun String.copyToClipboard() {
    val content = ClipboardContent()
    content.putString(this)
    Clipboard.getSystemClipboard().setContent(content)
}


fun String?.isStrictInteger(): Boolean = !isNullOrEmpty() && all { it.isDigit() }

fun String.isNumeric(): Boolean = toDoubleOrNull() != null

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

// this took avg ~600ns vs regex ~6-7k ns
fun String.replaceNonAlphanumericWithDash(): String = buildString(length) {
    // helper for multiple characters in a row are non-alphanumeric
    var lastWasNonAlpha = false

    for (ch in this@replaceNonAlphanumericWithDash) {
        if (ch.isLetterOrDigit()) {
            append(ch)
            lastWasNonAlpha = false
        } else if (!lastWasNonAlpha) {
            append('-')
            lastWasNonAlpha = true
        }
    }
}
