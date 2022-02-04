package afm.anime

enum class AnimeType {
    TV,
    OVA,
    MOVIE,
    SPECIAL,
    ONA,
    MUSIC,  // MAL but not the API i got this from
    UNKNOWN,
    ;

    companion object {
        @JvmStatic
        fun valueOfOrUnknown(name: String): AnimeType = values().firstOrNull { type ->
            name.equals(type.name, ignoreCase = true)
        } ?: UNKNOWN
    }
}
