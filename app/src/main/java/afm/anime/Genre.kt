package afm.anime

import afm.common.splitByCapitals

enum class GenreType {
    NORMAL, DEMOGRAPHIC, THEME;
}

enum class Genre(val id: Int,
                 val type: GenreType = GenreType.NORMAL,
                 @get:JvmName("isExplicit") val explicit: Boolean = false) {
    //<editor-fold desc="Genres">
    Action(1),
    Adventure(2),
    AvantGarde(5),
    AwardWinning(46),
    BoysLove(28),
    Cars(3, GenreType.THEME),  // Theme
    Comedy(4),
    Demons(6, GenreType.THEME),  // Theme
    Drama(8),
    Ecchi(9, explicit = true),
    Erotica(49, explicit = true),
    Fantasy(10),
    Game(11, GenreType.THEME),  // Theme
    GirlsLove(26),
    Gourmet(47),
    Harem(35, GenreType.THEME),  // Theme
    Hentai(12, explicit = true),
    Historical(13, GenreType.THEME),  // Theme
    Horror(14),
    Josei(43, GenreType.DEMOGRAPHIC),  // Demographics
    Kids(15, GenreType.DEMOGRAPHIC),  // Demographics
    Magic(16),
    MartialArts(17),
    Mecha(18, GenreType.THEME),  // Theme
    Military(38, GenreType.THEME),  // Theme
    Music(19, GenreType.THEME),  // Theme
    Mystery(7),
    Parody(20, GenreType.THEME),  // Theme
    Police(39, GenreType.THEME),  // Theme
    Psychological(40, GenreType.THEME),  // Theme
    Romance(22),
    Samurai(21, GenreType.THEME),  // Theme
    School(23, GenreType.THEME),  // Theme
    SciFi(24),
    Seinen(42, GenreType.DEMOGRAPHIC),  // Demographics
    Shoujo(25, GenreType.DEMOGRAPHIC),  // Demographics
    Shounen(27, GenreType.DEMOGRAPHIC),  // Demographics
    SliceOfLife(36),
    Space(29, GenreType.THEME),  // Theme
    Sports(30),
    SuperPower(31, GenreType.THEME),  // Theme
    Supernatural(37),
    Suspense(41),
    Vampire(32, GenreType.THEME),  // Theme
    WorkLife(48),
    ;
    //</editor-fold>

    override fun toString(): String = name.splitByCapitals().joinToString("-") +
            if (explicit) " (18+)" else ""

    companion object {
        fun parseGenreFromToString(toString: String?): Genre? =
            if (toString == null) null else values().firstOrNull {
                // ignore case for safety
                toString.equals(it.toString(), ignoreCase = true)
            }

        @JvmStatic
        fun valueOfFromName(name: String?): Genre? =
            if (name == null) null else values().firstOrNull {
                // ignore case for safety
                name.equals(it.name, ignoreCase = true)
            }

        @JvmStatic
        fun valuesOfType(type: GenreType): List<Genre> = values().filter {
            it.type == type
        }
    }
}
