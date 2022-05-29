package afm.anime

import afm.common.utils.remove
import afm.common.utils.splitByCapitals
import org.jsoup.Jsoup

enum class GenreType {
    NORMAL, DEMOGRAPHIC, THEME;
}

fun main() {
    fun getGenres() {
        val doc = Jsoup.connect("https://myanimelist.net/anime.php").get()
        val pairs = doc.select(".anime-manga-search")
            .first()!!
            .children()
            .take(8)
            .chunked(2)

        val result = pairs.map { (genreTypeE, genresE) ->
            val genreType = genreTypeE.text()
            // href="/anime/genre/1/Action"
            val genres: List<Triple<Int, String, String>> = genresE.select(".genre-name-link").map {
                val href = it.attr("href")
                val lastSlash = href.lastIndexOf('/')

                val genreId = href.substring(13, lastSlash).toInt()
                val name = href.substring(lastSlash + 1).remove('_').remove('-')

                Triple(genreId, name, genreType)
            }
            genres
        }.flatten()

        result.sortedBy { (id, name, major) ->
            name
        }.forEach { (id, name, major) ->
            print("$name($id")
            println(when (major) {
                "Explicit Genres" -> ", explicit = true),"
                "Themes" -> ", GenreType.THEME),  // Theme"
                "Demographics" -> ", GenreType.DEMOGRAPHIC),  // Demographics"
                else -> "),"
            })
        }
    }
    getGenres()
}

enum class Genre(val id: Int,
                 val type: GenreType = GenreType.NORMAL,
                 @get:JvmName("isExplicit") val explicit: Boolean = false,
) {
    //<editor-fold desc="Genres">
    Action(1),
    AdultCast(50, GenreType.THEME),  // Theme
    Adventure(2),
    Anthropomorphic(51, GenreType.THEME),  // Theme
    AvantGarde(5),
    AwardWinning(46),
    BoysLove(28),
    CGDCT(52, GenreType.THEME),  // Theme
    Childcare(53, GenreType.THEME),  // Theme
    CombatSports(54, GenreType.THEME),  // Theme
    Comedy(4),
    Crossdressing(81, GenreType.THEME),  // Theme
    Delinquents(55, GenreType.THEME),  // Theme
    Detective(39, GenreType.THEME),  // Theme
    Drama(8),
    Ecchi(9, explicit = true),
    Educational(56, GenreType.THEME),  // Theme
    Erotica(49, explicit = true),
    Fantasy(10),
    GagHumor(57, GenreType.THEME),  // Theme
    GirlsLove(26),
    Gore(58, GenreType.THEME),  // Theme
    Gourmet(47),
    Harem(35, GenreType.THEME),  // Theme
    Hentai(12, explicit = true),
    HighStakesGame(59, GenreType.THEME),  // Theme
    Historical(13, GenreType.THEME),  // Theme
    Horror(14),
    IdolsFemale(60, GenreType.THEME),  // Theme
    IdolsMale(61, GenreType.THEME),  // Theme
    Isekai(62, GenreType.THEME),  // Theme
    Iyashikei(63, GenreType.THEME),  // Theme
    Josei(43, GenreType.DEMOGRAPHIC),  // Demographics
    Kids(15, GenreType.DEMOGRAPHIC),  // Demographics
    LovePolygon(64, GenreType.THEME),  // Theme
    MagicalSexShift(65, GenreType.THEME),  // Theme
    MahouShoujo(66, GenreType.THEME),  // Theme
    MartialArts(17, GenreType.THEME),  // Theme
    Mecha(18, GenreType.THEME),  // Theme
    Medical(67, GenreType.THEME),  // Theme
    Military(38, GenreType.THEME),  // Theme
    Music(19, GenreType.THEME),  // Theme
    Mystery(7),
    Mythology(6, GenreType.THEME),  // Theme
    OrganizedCrime(68, GenreType.THEME),  // Theme
    OtakuCulture(69, GenreType.THEME),  // Theme
    Parody(20, GenreType.THEME),  // Theme
    PerformingArts(70, GenreType.THEME),  // Theme
    Pets(71, GenreType.THEME),  // Theme
    Psychological(40, GenreType.THEME),  // Theme
    Racing(3, GenreType.THEME),  // Theme
    Reincarnation(72, GenreType.THEME),  // Theme
    ReverseHarem(73, GenreType.THEME),  // Theme
    Romance(22),
    RomanticSubtext(74, GenreType.THEME),  // Theme
    Samurai(21, GenreType.THEME),  // Theme
    School(23, GenreType.THEME),  // Theme
    SciFi(24),
    Seinen(42, GenreType.DEMOGRAPHIC),  // Demographics
    Shoujo(25, GenreType.DEMOGRAPHIC),  // Demographics
    Shounen(27, GenreType.DEMOGRAPHIC),  // Demographics
    Showbiz(75, GenreType.THEME),  // Theme
    SliceOfLife(36),
    Space(29, GenreType.THEME),  // Theme
    Sports(30),
    StrategyGame(11, GenreType.THEME),  // Theme
    SuperPower(31, GenreType.THEME),  // Theme
    Supernatural(37),
    Survival(76, GenreType.THEME),  // Theme
    Suspense(41),
    TeamSports(77, GenreType.THEME),  // Theme
    TimeTravel(78, GenreType.THEME),  // Theme
    Vampire(32, GenreType.THEME),  // Theme
    VideoGame(79, GenreType.THEME),  // Theme
    VisualArts(80, GenreType.THEME),  // Theme
    Workplace(48, GenreType.THEME),  // Theme
    ;
    //</editor-fold>

    override fun toString(): String = name.splitByCapitals().joinToString("-") +
            if (explicit) " (18+)" else ""

    companion object {
        val values: Array<Genre> = values()
        private val ID_MAP = values.associateBy { it.id }

        fun parseGenreFromToString(toString: String?): Genre? = toString?.let {
            values.firstOrNull { genre ->
                // ignore case for safety
                toString.equals(genre.toString(), ignoreCase = true)
            }
        }

        @JvmStatic
        fun valueOfFromName(name: String): Genre = values.first { genre ->
            // ignore case for safety
            name.equals(genre.name, ignoreCase = true)
        }

        @JvmStatic
        fun valueOfFromId(id: Int): Genre = ID_MAP.getValue(id)

        @JvmStatic
        fun valuesOfType(type: GenreType): List<Genre> = values.filter { it.type == type }
    }
}
