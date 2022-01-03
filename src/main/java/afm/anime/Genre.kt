package afm.anime

import afm.utils.splitByCapitals
import java.util.EnumSet


// TODO: Demographic & Theme
/*
Might just keep demo & theme in here. Not sure whether to split them up like when showing search results show
them separately. Actually it's probably better to keep them here because some don't have a theme/demo.
 */
enum class Genre(val id: Int, val info: String? = null) {

    //<editor-fold desc="Genres">
    Action(1),
    Adventure(2),
    AvantGarde(5),
    AwardWinning(46),
    BoysLove(28),
    Cars(3),
    Comedy(4),
    Demons(6),
    Drama(8),
    Ecchi(9, "(18+)"),
    Erotica(49, "(18+)"),
    Fantasy(10),
    Game(11),
    GirlsLove(26),
    Gourmet(47),
    Harem(35),
    Hentai(12, "(18+)"),
    Historical(13),
    Horror(14),
    Josei(43),  // Demographics
    Kids(15),  // Demographics
    Magic(16),
    MartialArts(17),
    Mecha(18),
    Military(38),
    Music(19),
    Mystery(7),
    Parody(20),
    Police(39),
    Psychological(40),
    Romance(22),
    Samurai(21),
    School(23),
    SciFi(24),
    Seinen(42),  // Demographics
    Shoujo(25),  // Demographics
    Shounen(27),  // Demographics
    SliceOfLife(36),
    Space(29),
    Sports(30),
    SuperPower(31),
    Supernatural(37),
    Suspense(41),
    Vampire(32),
    WorkLife(48);
    //</editor-fold>

    override fun toString(): String {
        val sp = splitByCapitals(name)

        return sp.joinToString("-") +
                if (info == null) "" else " $info"
    }

    companion object {
        // TODO:
        private val demographics = EnumSet.of(Josei, Kids, Seinen, Shoujo, Shounen)

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
