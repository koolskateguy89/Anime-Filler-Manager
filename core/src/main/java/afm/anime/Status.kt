package afm.anime

enum class Status {
    FINISHED,
    AIRING,
    NOT_YET_AIRED,
    UNKNOWN,
    ;

    override fun toString(): String =
        name[0] + name.substring(1).replace('_', ' ').lowercase()

    companion object {
        @JvmStatic
        fun valueOfSafe(name: String): Status {
            return when (name) {
                "Finished" -> FINISHED
                "Airing" -> AIRING
                else -> NOT_YET_AIRED
            }
        }
    }

}
