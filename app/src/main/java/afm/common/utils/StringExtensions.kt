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

fun String.trimDashes(): String = trim { it == '-' }
