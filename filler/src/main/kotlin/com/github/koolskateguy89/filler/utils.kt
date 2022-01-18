package com.github.koolskateguy89.filler

import java.util.Collections

internal fun <K, V> Map<K, V>.unmodifiable(): Map<K, V> = Collections.unmodifiableMap(this)

internal fun <T> List<T>.unmodifiable(): List<T> = Collections.unmodifiableList(this)

internal fun IntRange.toIntArray(): IntArray {
    if (last < first)
        return IntArray(0)

    val result = IntArray(last - first + 1)
    var index = 0
    for (element in this)
        result[index++] = element
    return result
}

internal fun String.formatForAflUrl(): String {
    // replace all non-alphanumeric characters with a dash (which is what AFL does)
    var formattedName = this.lowercase().replaceNonAlphanumericWithDash()
    // name.toLowerCase().replaceAll("[^a-zA-Z0-9]+", "-")

    // get rid of leading/trailing dashes (due to formatting above)
    fun String.trimDashes(): String = trim { it == '-' }

    formattedName = formattedName.trimDashes()

    // if name includes a year, remove it
    if (formattedName.length > 6 && formattedName.takeLast(4).isNumeric()) {
        formattedName = formattedName.dropLast(4).trimDashes()
    }

    return formattedName
}

// this took avg ~600ns vs regex ~6-7k ns
internal fun String.replaceNonAlphanumericWithDash(): String = buildString(length) {
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

internal fun String.isNumeric(): Boolean = toDoubleOrNull() != null
