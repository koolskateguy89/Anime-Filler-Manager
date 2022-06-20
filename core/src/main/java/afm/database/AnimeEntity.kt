package afm.database

import afm.anime.EpisodeLength
import afm.anime.Genre
import afm.common.utils.emptyEnumSet
import afm.common.utils.splitIgnoreEmpty
import afm.database.MyListEntity.Companion.transform // (doesn't matter that it's MyListEntity)
import com.github.koolskateguy89.filler.Filler
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.EnumSet

private const val STUDIO_DELIMITER = "::"
private const val GENRE_DELIMITER = ","
private const val FILLER_DELIMITER = ","

sealed class AnimeEntity(id: EntityID<String>, table: AnimeTable) : Entity<String>(id) {
    var name = id.value
    var malId by table.malId
    var synopsis by table.synopsis
    var studios: Set<String> by table.studios.transform(studiosToString, stringToStudios)
    var genres: EnumSet<Genre> by table.genres.transform(genresToOrdinalString, ordinalStringToGenres)

    var imageUrl by table.imageUrl

    var fillers: Set<Filler> by table.fillers.transform(fillersToString, stringToFillers)
    var type by table.type
    var startYear by table.startYear
    var status by table.status

    var episodes by table.totalEps
    var currEp by table.currEp
    var episodeLength: EpisodeLength by table.episodeLength.transform(
        { it.mins },
        { EpisodeLength(it) }
    )

    var custom by table.custom
}

class MyListEntity(id: EntityID<String>) : AnimeEntity(id, MyListTable) {
    companion object : EntityClass<String, MyListEntity>(MyListTable)
}

class ToWatchEntity(id: EntityID<String>) : AnimeEntity(id, ToWatchTable) {
    companion object : EntityClass<String, ToWatchEntity>(ToWatchTable)
}

private fun String.removeBrackets() = drop(1).dropLast(1)

private val stringToStudios: (String) -> Set<String> = { str ->
    str.removeBrackets()
        .splitIgnoreEmpty(STUDIO_DELIMITER)
        .toSet()
}

private val studiosToString: (Set<String>) -> String = { studios ->
    studios.joinToString(
        separator = STUDIO_DELIMITER,
        prefix = "[",
        postfix = "]",
    )
}

private val ordinalStringToGenres: (String) -> EnumSet<Genre> = { str ->
    str.removeBrackets()
        .splitIgnoreEmpty(GENRE_DELIMITER)
        .mapTo(emptyEnumSet()) { ord -> Genre.values[ord.toInt()] }
}

private val genresToOrdinalString: (Collection<Genre>) -> String = { genres ->
    genres.joinToString(
        separator = GENRE_DELIMITER,
        prefix = "[",
        postfix = "]",
    ) { it.ordinal.toString() }
}

private val stringToFillers: (String) -> Set<Filler> = { str: String ->
    str.removeBrackets()
        .splitIgnoreEmpty(FILLER_DELIMITER)
        .map { Filler.valueOf(it) }
        .toSet()
}

private val fillersToString: (Collection<Filler>) -> String = { fillers ->
    fillers.joinToString(
        separator = FILLER_DELIMITER,
        prefix = "[",
        postfix = "]",
    )
}
