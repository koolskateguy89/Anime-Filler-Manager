@file:Suppress("SpellCheckingInspection")

package com.github.koolskateguy89.filler

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.test.assertContentEquals

// FIXME: mvn test finds these but doesnt run them

internal val emptyIntArray: IntArray = IntArray(0)

internal class NarutoTest {

    private lateinit var naruto: AnimeFiller

    @BeforeAll
    fun setUp() {
        naruto = AnimeFiller("https://www.animefillerlist.com/shows/naruto")
    }

    @Test
    fun getMangaCanon() {
        val expected = intArrayOf(
            1, 2, 3, 4, 5, 6, 8, 10, 11, 12, 13, 17, 22, 25, 31, 32, 33, 34, 35, 36, 42,
            48, 50, 51, 61, 62, 64, 65, 67, 68, 73, 75, 76, 77, 78, 79, 80, 81, 82, 84, 85, 86, 87, 88, 89, 90,
            91, 92, 93, 94, 95, 96, 107, 108, 109, 110, 111, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124,
            125, 128, 129, 132, 133, 134, 135,
        )
        assertContentEquals(expected, naruto.mangaCanon)
    }

    @Test
    fun getMixedCanonAndFiller() {
        val expected = intArrayOf(7, 9, 14, 15, 16, 18, 19, 20, 21, 23, 24, 27, 28, 29, 30, 37, 38, 39, 40, 41,
            43, 44, 45, 46, 47, 49, 52, 53, 54, 55, 56, 57, 58, 59, 60, 63, 66, 69, 70, 71, 72, 74, 83, 98, 100,
            112, 113, 114, 126, 127, 130, 131, 141, 142, 220
        )
        assertContentEquals(expected, naruto.mixedCanonAndFiller)
    }

    @Test
    fun getFiller() {
        val expected = intArrayOf(
            26, 97, 101, 102, 103, 104, 105, 106, 136, 137, 138, 139, 140, 143, 144, 145,
            146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164, 165,
            166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185,
            186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200, 201, 202, 203, 204, 205,
            206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219,
        )
        assertContentEquals(expected, naruto.filler)
    }

    @Test
    fun getAnimeCanon() {
        val expected = intArrayOf(99)
        assertContentEquals(expected, naruto.animeCanon)
    }

    @Test
    fun getAllFiller() {
        val expected = intArrayOf(7, 9, 14, 15, 16, 18, 19, 20, 21, 23, 24, 26, 27, 28, 29, 30, 37, 38, 39, 40,
            41, 43, 44, 45, 46,47, 49, 52, 53, 54, 55, 56, 57, 58, 59, 60, 63, 66, 69, 70, 71, 72, 74, 83, 97,
            98, 100, 101, 102, 103, 104, 105, 106, 112, 113, 114, 126, 127, 130, 131, 136, 137, 138, 139, 140,
            141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160,
            161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180,
            181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200,
            201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220
        )
        assertContentEquals(expected, naruto.allFiller)
        assertContentEquals(naruto.allFiller, (naruto.mixedCanonAndFiller + naruto.filler).sortedArray())
    }
}

internal class BeastarsTest {

    private lateinit var beastars: AnimeFiller

    @BeforeAll
    fun setUp() {
        beastars = AnimeFiller("https://www.animefillerlist.com/shows/beastars")
    }

    @Test
    fun getMangaCanon() {
        val expected = (1..24).toIntArray()
        assertContentEquals(expected, beastars.mangaCanon)
    }

    @Test
    fun getMixedCanonAndFiller() {
        assertContentEquals(emptyIntArray, beastars.mixedCanonAndFiller)
    }

    @Test
    fun getFiller() {
        assertContentEquals(emptyIntArray, beastars.filler)
    }

    @Test
    fun getAnimeCanon() {
        assertContentEquals(emptyIntArray, beastars.animeCanon)
    }

    @Test
    fun getAllFiller() {
        assertContentEquals(emptyIntArray, beastars.allFiller)
        assertContentEquals(beastars.allFiller, (beastars.mixedCanonAndFiller + beastars.filler).sortedArray())
    }

}
