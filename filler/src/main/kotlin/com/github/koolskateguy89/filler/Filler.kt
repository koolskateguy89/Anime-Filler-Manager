package com.github.koolskateguy89.filler

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import kotlin.math.min

// TODO: javadoc comments for publics

// TODO: async? how?
public object AnimeFillerList {

    private const val shows = "https://www.animefillerlist.com/shows"
    private const val showSelector = "#ShowList > div > ul > li > a"

    private val showNamesAndUrls: Map<String, String> by lazy {
        val doc = Jsoup.connect(shows)
            .timeout(8000)
            .get()

        val shows = doc.select(showSelector)
        return@lazy shows.associate { it.text() to it.absUrl("href") }.unmodifiable()
    }

    @JvmStatic
    public val allShows: Set<String> = showNamesAndUrls.keys

    @JvmStatic
    public val allShowsWithUrls: Map<String, String> = showNamesAndUrls

    // FIXME: make public once allFillers is fixed
    internal fun fillerRangesFor(name: String): AnimeFillerRange {
        TODO()
    }

    public fun fillerFor(name: String): AnimeFiller {
        return AnimeFiller("$shows/${name.formatForAflUrl()}")
    }

}

internal const val MANGA_CANON = ".manga_canon"
// jsoup doesn't properly deal with the /
// https://github.com/jhy/jsoup/issues/838
// internal const val MIXED = ".mixed_canon/filler"
internal const val MIXED = ":contains(mixed)"
internal const val FILLER = ".filler"
internal const val ANIME_CANON = ".anime_canon"

// TODO: async? how?
public class AnimeFiller internal constructor(url: String) {
    /**
     *
     */
    public val mangaCanon: IntArray

    /**
     *
     */
    public val mixedCanonAndFiller: IntArray

    /**
     *
     */
    public val filler: IntArray

    /**
     *
     */
    public val animeCanon: IntArray

    /**
     * A sorted combination of `mixedCanonAndFiller` and `filler`
     */
    public val allFiller: IntArray

    init {
        val doc = Jsoup.connect(url).get()
        val lists = doc.selectFirst("#Condensed")!!

        fun fillerList(selector: String): IntArray {
            return lists.select("div$selector > span.Episodes > a")
                .asSequence()
                .map { it.text() }
                .map {
                    val divPos = it.indexOf('-')
                    if (divPos == -1)
                        intArrayOf(it.toInt())
                    else {
                        val start = it.substring(0, divPos).toInt()
                        val end = it.substring(divPos + 1).toInt()
                        (start..end).toIntArray()
                    }
                }
                .fold(IntArray(0)) { a, b -> a + b }
        }

        mangaCanon = fillerList(MANGA_CANON)
        mixedCanonAndFiller = fillerList(MIXED)
        filler = fillerList(FILLER)
        animeCanon = fillerList(ANIME_CANON)
        allFiller = (mixedCanonAndFiller + filler).apply { sort() }
    }
}

private fun main() {
    val naruto = AnimeFillerList.fillerFor("naruto")
    //val naruto = AnimeFiller("https://www.animefillerlist.com/shows/naruto")
    println(naruto.mangaCanon.contentToString())
    println(naruto.mixedCanonAndFiller.contentToString())
    println(naruto.filler.contentToString())
    println(naruto.animeCanon.contentToString())
    println(naruto.allFiller.contentToString())
}


private typealias FillerList = List<Filler>

// TODO: async? how?
// FIXME: allFiller, then make public
internal class AnimeFillerRange internal constructor(name: String, url: String) {
    public val mangaCanon: FillerList
    public val mixedCanonAndFiller: FillerList
    public val filler: FillerList
    public val animeCanon: FillerList
    public val allFiller: FillerList

    init {
        val doc = Jsoup.connect(url).get()
        val lists = doc.selectFirst("#Condensed")!!
        //printSynonyms(doc, suffix = url.substringAfterLast('/'))

        fun fillerList(selector: String): FillerList {
            return lists.select("div$selector > span.Episodes > a")
                .map { Filler.valueOf(it.text()) }
                .unmodifiable()
        }

        mangaCanon = fillerList(MANGA_CANON)
        mixedCanonAndFiller = fillerList(MIXED)
        filler = fillerList(FILLER)
        animeCanon = fillerList(ANIME_CANON)
        allFiller = mixedCanonAndFiller + filler
    }
}

// can't just add & sort because e.g. 20-30 in mixed and 31 in filler, they dont get 'merged'
private fun allFiller(mixed: FillerList, filler: FillerList): FillerList {
    val episodes = mutableListOf<Int>()

    mixed.asSequence().map { it.toIntRange().toList() }.flatten().toCollection(episodes)
    filler.asSequence().map { it.toIntRange().toList() }.flatten().toCollection(episodes)
    // 2 pointer
    var mixedI = 0
    var fillerI = 0
    while (mixedI < mixed.size && fillerI < filler.size) {
        val mix = mixed[mixedI]
        val fill = filler[fillerI]
        val min = min(mix.start, fill.start)

        TODO("not sure what to do that will work")
    }

    episodes.sort()
    TODO("how do i impl this?")
}


public data class Filler(val start: Int, val end: Int) : Comparable<Filler> {

    public operator fun contains(n: Int): Boolean = start <= n && n <= end

    public fun toIntRange(): IntRange = start..end

    public override fun toString(): String = if (start == end) end.toString() else "$start-$end"

    // This smaller -> negative result
    public override operator fun compareTo(other: Filler): Int =
        if (start != other.start) start - other.start else end - other.end

    public companion object {

        @JvmStatic
        public fun valueOf(start: Int, end: Int = start): Filler {
            return Filler(start, end)
        }

        @JvmStatic
        public fun valueOf(s: String): Filler {
            val divPos = s.indexOf('-')

            // single episode filler
            if (divPos == -1)
                return valueOf(s.toInt())

            val start = s.substring(0, divPos).toInt()
            val end = s.substring(divPos + 1).toInt()
            return valueOf(start, end)
        }

        @JvmStatic
        public fun getFillers(name: String): List<Filler> {
            try {
                // replace all non-alphanumeric characters with a dash (which is what AFL does)
                val doc = Jsoup.connect("https://www.animefillerlist.com/shows/${name.formatForAflUrl()}").get()
                val fillers = doc.select("div.filler > span.Episodes > a")

                return fillers.map { valueOf(it.text()) }

            } catch (io: IOException) {
                // the page doesn't exist, likely the MAL name is different to the AFL name
                return emptyList()
            }
        }
    }
}

private fun printSynonyms(doc: Document, suffix: String) {
    // title
    doc.selectFirst("#node-13724 > div.Details.clearfix > div.Right > h1")?.let {
        if (it.text().formatForAflUrl() != suffix)
            println("\"${it.text()}\" to \"$suffix\",")
    }
    // "alt title"
    doc.selectFirst("#node-13724 > div.Details.clearfix > div.Right > h2:nth-child(4)")?.let {
        if (it.text().formatForAflUrl() != suffix)
            println("\"${it.text()}\" to \"$suffix\",")
    }
}

// TODO: map containing [synonyms -> AFL url]
private val synonymsMap: Map<String, String> = run {
    // if the value is not in the map, return the key formatted for AFL
    class SynonymsMap(private val map: Map<String, String>) : Map<String, String> by map {
        override fun get(key: String): String {
            return map[key] ?: key.formatForAflUrl()
        }
    }

    return@run SynonymsMap(
        mapOf(
            "Naruto: Shippuden" to "naruto-shippuden",
            "A Certain Magical Index (Toaru Majutsu No Index)" to "certain-magical-index",
            "A Certain Magical Index Filler List" to "certain-magical-index",
            "Toaru Majutsu No Index" to "certain-magical-index",
            "A Certain Scientific Accelerator (Toaru Kagaku no Accelerator)" to "sh%C5%8Dnan-pure-love-gang",
            "A Certain Scientific Railgun (Toaru Kagaku No Railgun)" to "a-certain-scientific-railgun",
            "A Sister's All You Need" to "super-dragon-ball-heroes",
            "Ace Attorney (Gyakuten Saiban)" to "ace-attorney",
            "Ace of the Diamond" to "ace-diamond",
            "Ace of the Diamond: Act II" to "ace-diamond-act-ii",
            "Assassination Classroom (Ansatsu Kyoushitsu)" to "ansatsu-kyoushitsu-assassination-classroom",
            "Attack on Titan (Shingeki no Kyojin)" to "attack-titan",
            "Attack on Titan OADs" to "attack-titan-ova-0",
            "Berserk (Berserk Kenpuden)" to "berserk",
            "Berserk (2016)" to "berserk-2016",
            "Berserk: The Golden Age Arc" to "berserk-golden-age-arc",
            "Beyblade Burst" to "beyblade-burst-all-seasons",
            "Black Butler (Kuroshitsuji)" to "black-butler",
            "Blade Dance of the Elementalers" to "seirei-tsukai-no-blade-dance",
            "Blade of the Immortal (2019)" to "blade-immortal-2019",
            "Blood Blockade Battlefront (Kekkai Sensen)" to "blood-blockade-battlefront",
            "Blue Exorcist (Ao no Exorcist)" to "blue-exorcist",
            "Bobobo-bo Bo-bobo" to "bobobo-bo-bobo",
            "Boruto: Naruto Next Generations (Manga Only)" to "boruto-naruto-next-generations-manga-canon",
            "Boruto: Naruto the Movie" to "boruto-naruto-movie",
            "BURN THE WITCH" to "burn-witch",
            "Case Closed (Detective Conan)" to "detective-conan",
            "Cells at Work! (Hataraku Saibou)" to "cells-work",
            "Cells at Work! CODE BLACK (Hataraku Saibou Black)" to "cells-work-code-black",
            "Classroom of the Elite" to "classroom-elite",
            "Cobra: The Animation" to "cobra-animation",
            "Darwin's Game" to "darwins-game",
            "Demon Slayer: Kimetsu no Yaiba the Movie: Mugen Train" to "demon-slayer-kimetsu-no-yaiba-movie-mugen-train",
            "Den-Noh Coil" to "dennou-coil",
            "Devilman" to "devilman-birth",
            "Digimon Adventure:" to "digimon-adventure-2020",
            "Don’t Toy With Me, Miss Nagatoro (Ijiranaide, Nagatoro-san)" to "don%E2%80%99t-toy-me-miss-nagatoro",
            "Dorohedoro" to "dragon-ball-super-broly",
            "Dragon Ball Super: Broly" to "dragon-ball-super-broly-0",
            "Dragon Quest: The Adventure of Dai" to "dragon-quest-adventure-dai",
            "Emma: A Victorian Romance (Eikoku Koi Monogatari Emma)" to "emma-victorian-romance",
            "ERASED (Boku Dake ga Inai Machi)" to "erased",
            "Fate/Kaleid Liner Prisma Illya" to "fatekaleid-liner-prisma-illya",
            "Fate/stay night" to "fatestay-night",
            "Fate/stay night [Unlimited Blade Works]" to "fatestay-night-unlimited-blade-works",
            "Fighting Spirit (Hajime no Ippo)" to "fighting-spirit",
            "Fire Force (Enen no Shouboutai)" to "fire-force",
            "Fist of the North Star" to "hokuto-no-ken",
            "Food Wars! (Shokugeki no Souma)" to "shokugeki-no-soma",
            "Food Wars! The Fifth Plate (Shokugeki no Souma: Gou no Sara)" to "food-wars-fifth-plate",
            "Food Wars! The Fourth Plate (Shokugeki no Souma: Shin no Sara)" to "food-wars-fourth-plate",
            "Food Wars! The Second Plate (Shokugeki no Souma: Ni no Sara)" to "food-wars-second-plate-1",
            "Food Wars! The Third Plate (Shokugeki no Souma: San no Sara)" to "food-wars-third-plate-1",
            "Fruits Basket (Furuba)" to "fruits-basket-2001",
            "Fruits Basket (2019)" to "fruits-basket-2019",
            "Ghost in the Shell: Arise" to "ghost-shell-arise",
            "Ghost In the Shell: Stand Alone Complex" to "ghost-shell-stand-alone-complex",
            "Girlfriend, Girlfriend (Kanojo mo Kanojo)" to "girlfriend-girlfriend",
            "Haikyu!!" to "haikyuu",
            "HAPPY-GO-LUCKY DAYS (Dounika no Hibi)" to "happy-go-lucky-days",
            "Hayate the Combat Butler (Hayate no Gotoku!)" to "hayate-combat-butler",
            "Heaven's Lost Property" to "heavens-lost-property",
            "Hell Girl (Jigoku Shōjo Girl)" to "hell-girl",
            "Highschool DxD" to "high-school-dxd",
            "Highschool of the Dead" to "highschoool-dead",
            "Hunter × Hunter" to "hunter-x-hunter-1999",
            "Hunter × Hunter (2011)" to "hunter-x-hunter",
            "Is It Wrong to Try to Pick Up Girls in a Dungeon? (Dungeon ni Deai wo Motomeru no wa Machigatteiru Darou ka)" to "it-wrong-try-pick-girls-dungeon",
            "Is It Wrong to Try to Pick Up Girls In a Dungeon? On the Side: Sword Oratoria" to "it-wrong-try-pick-girls-dungeon-side-sword-oratoria",
            "JoJo's Bizarre Adventure (OVA)" to "jojos-biarre-adenture-ova",
            "JoJo's Bizarre Adventure (TV)" to "jojos-bizarre-adventure-tv",
            "K (K Project)" to "k",
            "Kabaneri of the Iron Fortress" to "kabaneri-iron-fortress",
            "Kamisama Kiss (Kamisama Hajimemashita)" to "kamisama-kiss",
            "KenIchi: The Mightiest Disciple" to "kenIchi-mightiest-disciple",
            "Komi Can’t Communicate (Komi-san wa, Comyushou desu.)" to "blue-period-0",
            "KonoSuba: God's Blessing on This Wonderful World! (Kono Subarashii Sekai ni Shukufuku wo!)" to "konosuba-gods-blessing-wonderful-world",
            "Kuroko’s Basketball (Kuroko no Basket)" to "kuroko%E2%80%99s-basketball",
            "Lord Marksman and Vanadis (Madan No Ou to Vanadis)" to "madan-no-ou-vanadis-lord-marksman-and-vanadis",
            "Lupin the Third Part I (Lupin III: Part I)" to "lupin-third-part-i",
            "Made in Abyss" to "made-abyss",
            "Magi" to "magi-labyrinth-magic",
            "Magi: Adventure of Sinbad" to "magi-adventure-sinbad-tv",
            "Maid-Sama! (Kaichou wa maid sama)" to "maid-sama",
            "Miraculous - Tales of Ladybug & Cat Noir" to "miraculous-ladybug",
            "Miss Kobayashi's Dragon Maid" to "miss-kobayashis-dragon-maid",
            "Mob Psycho 100" to "mob-psycho-100",
            "Mobile Suit Gundam (Kidou Senshi Gundam)" to "mobile-suit-gundam",
            "Mobile Suit Gundam ZZ (Kidou Senshi Gundam ZZ)" to "mobile-suit-gundam-zz",
            "Mobile Suit Zeta Gundam (Kidou Senshi Zeta Gundam)" to "mobile-suit-zeta-gundam",
            "Monogatari" to "monogatari-series",
            "My Hero Academia (Boku no Hero Academia)" to "my-hero-academia",
            "My Next Life as a Villainess: All Routes Lead to Doom! (Bakarina)" to "my-next-life-villainess-all-routes-lead-doom",
            "Nadia: The Secret of Blue Water (Fushigi no Umi no Nadia)" to "nadia-secret-blue-water",
            "Negima OVA/OAD" to "negima-ovaoad",
            "Negima! (Mahou Sensei Negima!)" to "magister-negi-magi",
            "Negima!? (Mahou Sensei Negima?!)" to "noragami-aragoto",
            "Panty & Stocking with Garterbelt" to "tokyo-ghoul-%E2%88%9A-5",
            "Parasyte -the Maxim- (Kiseijuu)" to "parasyte-maxim",
            "Platinum End - Duplicate" to "your-eternity-0",
            "Pokémon" to "pokemon",
            "Pokemon - Duplicate" to "pok%C3%A9mon-series-xyz",
            "Pokémon Advanced Generation" to "pok%C3%A9mon-advanced-generation",
            "Pokémon Black & White (Pocket Monsters: Best Wishes)" to "pok%C3%A9mon-black-and-white",
            "Pokémon Diamond and Pearl" to "pok%C3%A9mon-diamond-and-pearl",
            "Pokémon Journeys: The Series" to "pok%C3%A9mon-journeys",
            "Pokémon Origins (Pocket Monsters: The Origin)" to "pok%C3%A9mon-origins-0",
            "Pokémon the Series: Sun & Moon" to "pok%C3%A9mon-sun-and-moon",
            "Pokémon the Series: XY" to "pok%C3%A9mon-x-and-y",
            "Pokémon: Original series" to "pok%C3%A9mon-original-series",
            "Project ARMS (ARMS)" to "project-arms",
            "Ranma ½" to "ranma-%C2%BD",
            "Rave Master (Groove Adventure Rave)" to "rave-master",
            "Re:ZERO -Starting Life in Another World- (Re: Zero- Kara Hajimeru Isekai Seikatsu)" to "re-zero-starting-life-another-world",
            "Reborn! (Katekyō Hitman Reborn!)" to "katekyo-hitman-reborn",
            "Record of Ragnarok (Shuumatsu no Walküre)" to "record-ragnarok",
            "Redo of Healer (Kaifuku Jutsushi no Yarinaoshi)" to "redo-healer",
            "Saga of Tanya the Evil" to "sage-tanya-evil",
            "Saga of Tanya the Evil - Duplicate" to "yakusoku-no-neverland",
            "Sailor Moon (Bishoujo Sailor Moon)" to "sailor-moon",
            "Seraph of the End: Vampire Reign (Owari no Seraph)" to "seraph-end",
            "Sgt. Frog (Keroro Gunsou)" to "keroro-gunsou-sgtfrog",
            "Shaman King (2021)" to "shaman-king-2021",
            "Slam Dunk" to "slam-dunk-0",
            "Soul Hunter (Hoshin Engi)" to "soul-hunter",
            "Steins;Gate" to "steinsgate",
            "Super Dragon Ball Heroes" to "dragon-ball-heroes",
            "Sword Art Online Alternative: Gun Gale Online" to "sword-art-online-alternative-ggo",
            "Terror in Resonance (Zankyou no Terror)" to "terror-resonance",
            "That Time I Got Reincarnated as a Slime (Tensei shitara Slime Datta Ken)" to "time-i-got-reincarnated-slime",
            "The Ancient Magus' Bride (Mahoutsukai no Yome)" to "ancient-magus-bride",
            "The Asterisk War (Gakusen no Asterisk)" to "asterisk-war",
            "The Devil Is a Part-Timer! (Hataraku Maou-Sama!)" to "devil-part-timer",
            "The Familiar Of Zero (Zero No Tsukaima)" to "familiar-zero",
            "The Future Diary (Mirai Nikki)" to "future-diary",
            "The God of High School (Gat Obeu Hai Seukul)" to "god-high-school",
            "The Heroic Legend of Arslan (Arslan Senki)" to "heroic-legend-arslan",
            "The Melancholy of Haruhi Suzumiya (Suzumiya Haruhi no Yuuutsu)" to "melancholy-haruhi-suzumiya",
            "The Prince of Tennis (Tenisu no Ōjisama)" to "prince-tennis-0",
            "The Promised Neverland (Yakusoku no Neverland)" to "promised-neverland",
            "The Rising of the Shield Hero (Tate no Yuusha no Nariagari)" to "rising-shield-hero",
            "The Seven Deadly Sins (Nanatsu no Taizai)" to "nanatsu-no-taizai",
            "Thus Spoke Kishibe Rohan" to "chrono-crusade",
            "To Love Ru" to "love-ru",
            "To Love Ru: Darkness" to "love-ru-darkness",
            "To The Abandoned Sacred Beasts (Katsute Kami Datta Kemono-tachi e)" to "abandoned-sacred-beasts",
            "To Your Eternity (Fumetsu no Anata e)" to "your-eternity",
            "Tokyo Ghoul:re" to "tokyo-ghoul-re-0",
            "We Never Learn!: BOKUBEN (Bokutachi wa Benkyou ga Dekinai)" to "we-never-learn-bokuben",
            "When They Cry (Higurashi no naku koro ni)" to "when-they-cry",
            "Yona of the Dawn (Akatsuki no Yona)" to "akatsuki-no-yona",
            "Your lie in April (Shigatsu wa Kimi no Uso)" to "your-lie-april",
            "Yu Yu Hakusho" to "yuyu-hakusho",
            "Yu-Gi-Oh! 5D's" to "yu-gi-oh-5ds",
            "YU-NO: A Girl Who Chants Love at the Bound of This World" to "yu-no-girl-who-chants-love-bound-world",
            "Zatch Bell! (Konjiki no Gash Bell!!)" to "zatch-bell",
        )
    )
}
