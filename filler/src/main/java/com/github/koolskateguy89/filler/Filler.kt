package com.github.koolskateguy89.filler

import com.google.common.collect.HashBasedTable
import org.jsoup.Jsoup
import java.io.IOException

data class Filler(val start: Int, val end: Int) : Comparable<Filler> {

    operator fun contains(n: Int): Boolean = n in start..end

    override fun toString(): String = if (start == end) end.toString() else "$start-$end"

    // This smaller -> negative result
    override operator fun compareTo(other: Filler): Int =
        if (start != other.start) start - other.start else end - other.end

    companion object {
        // Start, End, Object
        private val CACHE = HashBasedTable.create<Int, Int, Filler>()

        private fun of(start: Int, end: Int = start): Filler {
            var cached = CACHE[start, end]

            if (cached == null) {
                cached = Filler(start, end)
                CACHE.put(start, end, cached)
            }

            return cached
        }

        @JvmStatic
        fun valueOf(s: String): Filler {
            val divPos = s.indexOf('-')

            // single episode filler
            if (divPos == -1)
                return of(s.toInt())

            val start = s.substring(0, divPos).toInt()
            val end = s.substring(divPos + 1).toInt()
            return of(start, end)
        }

        @JvmStatic
        fun getFillers(name: String): List<Filler> {
            try {
                // replace all non-alphanumeric characters with a dash (which is what AFL does)
                val doc = Jsoup.connect("https://www.animefillerlist.com/shows/${name.formatForAflUrl()}").get()
                val fillerElem = doc.select("div.filler > span.Episodes")

                return if (fillerElem.isEmpty())
                    emptyList()
                else
                    fillerElem.first()!!.text().split(", ").map(::valueOf)

            } catch (io: IOException) {
                // the page doesn't exist, likely the MAL name is different to the AFL name
                return emptyList()
            }
        }
    }
}

private fun String.isNumeric(): Boolean = toDoubleOrNull() != null

// this took avg ~600ns vs regex ~6-7k ns
fun String.replaceNonAlphanumericWithDash(): String {
    val sb = StringBuilder()

    // helper for multiple characters in a row are non-alphanumeric
    var lastWasNonAlpha = false

    for (ch in this) {
        if (ch.isLetterOrDigit()) {
            sb.append(ch)
            lastWasNonAlpha = false
        } else if (!lastWasNonAlpha) {
            sb.append('-')
            lastWasNonAlpha = true
        }
    }
    return sb.toString()
}

private fun String.formatForAflUrl(): String {
    // replace all non-alphanumeric characters with a dash (which is what AFL does)
    var formattedName = this.lowercase().replaceNonAlphanumericWithDash()
    // name.toLowerCase().replaceAll("[^a-zA-Z0-9]+", "-")

    // get rid of leading/trailing dashes (due to formatting above)
    fun String.trimDashes(): String = trim { it == '-' }

    formattedName = formattedName.trimDashes()

    // basically if name includes a year, remove it
    if (formattedName.length > 6 && formattedName.takeLast(4).isNumeric()) {
        formattedName = formattedName.dropLast(4)
        formattedName = formattedName.trimDashes()
    }

    return formattedName
}
