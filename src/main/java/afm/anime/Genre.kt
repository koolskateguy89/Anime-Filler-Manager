package afm.anime

import afm.utils.splitByCapitals


// TODO: rename to smthn better, it's something that can be used to search
sealed interface GenreType {
    val id: Int
    val name: String  // defined by enum

    companion object {
        init {
        }
    }
}


// TODO: Demographic & Theme
/*
Might just keep demo & theme in here. Not sure whether to split them up like when showing search results show
them separately. Actually it's probably better to keep them here because some don't have a theme/demo.
 */
enum class Genre(override val id: Int, private val info: String? = null) : GenreType {

    //<editor-fold desc="Genres">
    Action(1),
    Adventure(2),
    AvantGarde(5),
    AwardWinning(46),
    BoysLove(28),
    Cars(3),  // Theme
    Comedy(4),
    Demons(6),  // Theme
    Drama(8),
    Ecchi(9, "(18+)"),
    Erotica(49, "(18+)"),
    Fantasy(10),
    Game(11),  // Theme
    GirlsLove(26),
    Gourmet(47),
    Harem(35),  // Theme
    Hentai(12, "(18+)"),
    Historical(13),  // Theme
    Horror(14),
    Josei(43),  // Demographics
    Kids(15),  // Demographics
    Magic(16),
    MartialArts(17),
    Mecha(18),  // Theme
    Military(38),  // Theme
    Music(19),  // Theme
    Mystery(7),
    Parody(20),  // Theme
    Police(39),  // Theme
    Psychological(40),  // Theme
    Romance(22),
    Samurai(21),  // Theme
    School(23),  // Theme
    SciFi(24),
    Seinen(42),  // Demographics
    Shoujo(25),  // Demographics
    Shounen(27),  // Demographics
    SliceOfLife(36),
    Space(29),  // Theme
    Sports(30),
    SuperPower(31),  // Theme
    Supernatural(37),
    Suspense(41),
    Vampire(32),  // Theme
    WorkLife(48);
    //</editor-fold>

    override fun toString(): String {
        val sp = name.splitByCapitals()

        return sp.joinToString("-") +
                if (info == null) "" else " $info"
    }

    companion object {
        fun parseGenreFromToString(toString: String?): Genre? {
            if (toString == null)
                return null

            return values().firstOrNull {
                // ignore case for safety
                toString.equals(it.toString(), ignoreCase = true)
            }
        }

        @JvmStatic
        fun getGenre(name: String?): Genre? {
            if (name == null)
                return null

            return values().firstOrNull {
                // ignore case for safety
                name.equals(it.name, ignoreCase = true)
            }
        }
    }
}

private enum class Theme(override val id: Int) : GenreType {
    Cars(3),
    Demons(6),
    Game(11),
    Harem(35),
    Historical(13),
    Mecha(18),
    Military(38),
    Music(19),
    Parody(20),
    Police(39),
    Psychological(40),
    Samurai(21),
    School(23),
    Space(29),
    SuperPower(31),
    Vampire(32),
    ;
}

private enum class Demographic(override val id: Int) : GenreType {
    Josei(43),
    Kids(15),
    Seinen(42),
    Shoujo(25),
    Shounen(27),
    ;
}
