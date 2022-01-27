package afm.database

import afm.anime.Anime
import afm.anime.EpisodeLength
import afm.anime.Genre
import afm.common.utils.emptyEnumSet
import afm.common.utils.splitIgnoreEmpty
import afm.database.MyListEntity.Companion.transform
import com.github.koolskateguy89.filler.Filler
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.EnumSet

private const val STUDIO_DELIMITER = "::"
private const val GENRE_DELIMITER = ","
private const val FILLER_DELIMITER = ","

sealed class AnimeEntity private constructor(id: EntityID<String>, table: AnimeTable) : Entity<String>(id) {
    val name = id.value
    val malId by table.malId
    val synopsis by table.synopsis
    val studios: Set<String> by table.studios.transform(studiosToString, stringToStudios)

    val genres: EnumSet<Genre> by table.genres.transform(genresToOrdinalString, ordinalStringToGenres)

    val imageUrl by table.imageUrl

    val fillers: Set<Filler> by table.fillers.transform(fillersToString, stringToFillers)
    val type by table.type
    val startYear by table.startYear
    val status by table.status

    val episodes by table.totalEps
    var currEp by table.currEp

    val episodeLength: EpisodeLength by table.episodeLength.transform(
        { it.mins },
        { EpisodeLength(it) }
    )

    val custom: Boolean by table.custom

    fun toAnime(): Anime = with (Anime.builder(name)) {
        setId(malId)
        setSynopsis(synopsis)
        setStudios(studios)

        setGenres(genres)

        setImageURL(imageUrl)

        addFillers(fillers)
        setAnimeType(type)
        setStartYear(startYear)
        setStatus(status)

        setEpisodes(episodes)
        setCurrEp(currEp)
        setEpisodeLength(episodeLength)

        setCustom(custom)

        build()
    }
}

class MyListEntity(id: EntityID<String>) : AnimeEntity(id, MyListTable) {
    companion object : EntityClass<String, MyListEntity>(MyListTable)
    override fun toString(): String {
        return "MyListEntry(name='$name', malId=$malId, synopsis='$synopsis', studios=$studios, genres=$genres, imageUrl='$imageUrl', fillers=$fillers, type=$type, startYear=$startYear, status=$status, episodes=$episodes, currEp=$currEp, episodeLength=$episodeLength, custom=$custom)"
    }
}

class ToWatchEntity(id: EntityID<String>) : AnimeEntity(id, ToWatchTable) {
    companion object : EntityClass<String, ToWatchEntity>(ToWatchTable)
    override fun toString(): String {
        return "ToWatchEntry(name='$name', malId=$malId, synopsis='$synopsis', studios=$studios, genres=$genres, imageUrl='$imageUrl', fillers=$fillers, type=$type, startYear=$startYear, status=$status, episodes=$episodes, currEp=$currEp, episodeLength=$episodeLength, custom=$custom)"
    }
}

// TODO: Anime.toAnimeModel ext function (or smthn like that, use AnimeModel.new {})


private fun String.removeBrackets() = drop(1).dropLast(1)

private val stringToStudios = { str: String ->
    str.removeBrackets()
        .splitIgnoreEmpty(STUDIO_DELIMITER)
        .toSet()
}

private val studiosToString = { studios: Set<String> ->
    studios.joinToString(
        separator = STUDIO_DELIMITER,
        prefix = "[",
        postfix = "]",
    )
}

private val ordinalStringToGenres = { str: String ->
    str.removeBrackets()
        .splitIgnoreEmpty(GENRE_DELIMITER)
        .mapTo(emptyEnumSet<Genre>()) { ord -> Genre.values[ord.toInt()] }
}

private val genresToOrdinalString = { genres: Collection<Genre> ->
    genres.joinToString(
        separator = GENRE_DELIMITER,
        prefix = "[",
        postfix = "]",
    ) { it.ordinal.toString() }
}

private val stringToFillers = { str: String ->
    str.removeBrackets()
        .splitIgnoreEmpty(FILLER_DELIMITER)
        .map { Filler.valueOf(it) }
        .toSet()
}

private val fillersToString = { fillers: Collection<Filler> ->
    fillers.joinToString(
        separator = FILLER_DELIMITER,
        prefix = "[",
        postfix = "]",
    )
}
