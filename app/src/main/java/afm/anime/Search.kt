package afm.anime

import afm.Main
import afm.common.utils.currentYear
import afm.common.utils.inJar
import afm.common.utils.remove
import javafx.application.Platform
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.io.IOException
import java.net.UnknownHostException
import java.util.EnumSet
import javax.net.ssl.SSLHandshakeException
import kotlin.math.min

private val builder = Anime.builder()

private const val MAL_URL = "https://myanimelist.net"
private const val GENRE_URL = "$MAL_URL/anime/genre" //+"/${genre.getId()}"
private const val TIMEOUT_MILLIS = 8000

// rename to smthn different? like extractor?
private interface Selector {
    fun extractFrom(animeElem: Element): Any?
}

// a Selector that returns multiple things (destructible)
private interface MultipleSelector : Selector

// https://jsoup.org/cookbook/extracting-data/selector-syntax
private const val ANIME_ELEMS =
    "#content > div.js-categories-seasonal.js-block-list.tile.mt16 > div > div.seasonal-anime.js-seasonal-anime"

private object NameAndId : Selector {
    private const val cssQuery = "div:nth-child(1) > div.title > div.title-text > h2.h2_anime_title > a"

    /*
     * Link will be in form:
     * https://myanimelist.net/anime/[ID]/[TITLE]
     */
    private fun getIdFromURl(url: String): Int {
        val start = 30 //where ID starts
        val end = url.indexOf('/', start + 1)
        return url.substring(start, end).toIntOrNull() ?: -1
    }

    override fun extractFrom(animeElem: Element): Pair<String, Int> {
        val nameElem: Element = animeElem.selectFirst(cssQuery)!!

        val name = nameElem.text()
        val url = nameElem.absUrl("href")
        val id = getIdFromURl(url)

        return name to id
    }
}

private object Genres : Selector {
    override fun extractFrom(animeElem: Element): EnumSet<Genre> {
        val ids = animeElem.attr("data-genre").split(",")

        val result = EnumSet.noneOf(Genre::class.java)
        ids.map { Genre.valueOfFromId(it.toInt()) }
            .toCollection(result)
        return result
    }
}

// TODO: collapse into 1 MultipleSelector "Properties" and return a Triple<String, List<String, EnumSet<Genre>> ?
private object Synopsis : Selector {
    private const val cssQuery = "div.synopsis.js-synopsis"

    private lateinit var synopsisElem: Element

    override fun extractFrom(animeElem: Element): String {
        synopsisElem = animeElem.selectFirst(cssQuery)!!
        return synopsisElem.selectFirst("p")!!.text()
    }

    object Studios : Selector {
        private const val cssQuery = "div.properties > div:nth-child(1) > span.item > a"
        override fun extractFrom(animeElem: Element): Set<String> {
            val studioElems = synopsisElem.select(cssQuery)
            return studioElems.map { it.text() }.toSortedSet()
        }
    }

    object ThemesAndDemographics : Selector {
        /* selects the properties after the 1st (studios), whose text doesn't contain
         * "Source" (case insensitive), thus only selecting themes and demographics
         * (if any)
         */
        private const val onlyThemeAndDemos = ":gt(1):not(:contains(Source))"
        private const val cssQuery =
            "div.properties > div$onlyThemeAndDemos > span.item"

        override fun extractFrom(animeElem: Element): EnumSet<Genre> {
            val result = EnumSet.noneOf(Genre::class.java)

            val names = synopsisElem.select(cssQuery)
            names.map { Genre.valueOfFromName(it.text().remove(" ")) }
                 .toCollection(result)

            return result
        }
    }

}

private object Infos : MultipleSelector {
    private const val cssQuery = "div:nth-child(1) > div.prodsrc > div.info > span"

    data class Info(
        val animeType: AnimeType,
        val startYear: Int,
        val status: Status,
        val eps: Int,
        val epLength: EpisodeLength,
    )

    override fun extractFrom(animeElem: Element): Info {
        val infos: Elements = animeElem.select(cssQuery)

        val (type, startYear) = infos[0].text().split(", ")

        val airingStatus = infos[1].text()

        val (epsElem, lengthElem) = infos[2].children()
        val eps: Int =
            epsElem.text().substringBefore(' ').toIntOrNull() ?: Anime.NOT_FINISHED

        val episodeLength = lengthElem.text().substringBefore(' ').toInt()

        return Info(
            AnimeType.valueOfOrUnknown(type),
            startYear.toInt(),
            Status.valueOfSafe(airingStatus),
            eps,
            EpisodeLength(episodeLength),
        )
    }
}

private object ImageUrl : Selector {
    private const val cssQuery = "div.image > a:nth-child(1) > img"
    override fun extractFrom(animeElem: Element): String =
        animeElem.selectFirst(cssQuery)!!.absUrl("data-src")
}


/*
 * Search by genre (there HAS to be a genre in order to search)
 *
 * steps:
 *
 *  - search for every genre
 *	- ignore all anime whose name does not contain the name the user entered (if entered)
 *	- ignore all anime whose genres does not contain all genres the user selected
 *	- ignore all anime whose season is not one of the seasons the user selected (if any selected)
 *	- ignore all anime whose studio does not contain the studio the the user entered (if entered)
 *	- ignore all anime who don't meet the minimum episodes requirement entered by the user (if entered)
 *		(any unfinished anime will pass)
 *
 *  - add any anime left to result list
 *  - if result contains less than 13 anime, search the next page
 *
 *
 *  The removeBecause[X] methods in this are in order of how they are used.
 */
class Search {
    /* search filters */
    var name: String? = null
    var studio: String? = null
    private val genres = EnumSet.noneOf(Genre::class.java)
    private val seasons = mutableSetOf<Season>()
    // TODO: new search filters
    var animeType: AnimeType? = null
    var startYear: Int? = null
        set(value) {
            field = value?.let { min(it, currentYear) }
        }
    var status: Status? = null
    var minEpisodes: Int? = null
        set(value) {
            field = value?.let { min(it, 72) }
        }

    /* class variables to assist with web scraping */
    private var page = 1
    private var reachedLastPage = false

    private val result = hashSetOf<Anime>()

    fun setGenres(genres: Collection<Genre>) {
        this.genres.clear()
        this.genres.addAll(genres)
    }

    fun setSeasons(set: Set<Season>) {
        seasons.clear()
        seasons.addAll(set)
    }

    fun search(): List<Anime> {
        /* this should be impossible because it's already taken care of
		   in SearchScreen */
        if (genres.isEmpty())
            throw Error(IllegalArgumentException("No genres selected for searching"))

        val searchWorked: Boolean = searchForEachGenre()

        if (searchWorked && !reachedLastPage && result.size < 13 /*&& (name == null)*/) {
            page++
            /* I don't want it to continue as other search() calls
			 * will sort - which only need to happen once */
            return search()
        }

        return result.sortedWith(Anime.SORT_BY_NAME)
    }

    // (and)
    // searches for each and every genre the user selected
    // returns if search 'worked'
    private fun searchForEachGenre(): Boolean {
        for (genre in genres) {
            val doc: Document
            try {
                doc = Jsoup.connect("$GENRE_URL/${genre.id}/page=$page")
                    //.cookie("search_view", "list") // use the list search view to be able to get start date
                    .timeout(TIMEOUT_MILLIS)
                    .get()
                scrapeDocument(doc)
            } catch (e: Exception) {
                when (e) {
                    // no internet connection
                    is UnknownHostException -> Platform.runLater {
                        Alert(AlertType.ERROR, "No internet connection").run {
                            initOwner(Main.getStage())
                            showAndWait()
                        }
                    }
                    // gone past last page
                    is HttpStatusException -> reachedLastPage = true
                    // likely caused by MAL being blocked or something
                    is SSLHandshakeException -> Platform.runLater {
                        Alert(AlertType.ERROR, "Could not connect to MyAnimeList").run {
                            initOwner(Main.getStage())
                            showAndWait()
                        }
                    }
                    is IOException -> if (!inJar) e.printStackTrace()
                    else -> throw e
                }
                return false
            }
        }

        return true
    }

    // Scrapes the document, adding all appropriate anime to result
    private fun scrapeDocument(doc: Document) =
        doc.select(ANIME_ELEMS).forEach {
            builder.reset()

            val (name, id) = NameAndId.extractFrom(it)
            if (removeBecauseName(name)) return@forEach
            builder.setName(name)
                   .setId(id)

            val synopsis: String = Synopsis.extractFrom(it)
            builder.setInfo(synopsis)

            val studios: Set<String> = Synopsis.Studios.extractFrom(it)
            if (removeBecauseStudio(studios)) return@forEach
            builder.setStudios(studios)

            val genres = EnumSet.noneOf(Genre::class.java)
            genres.addAll(Genres.extractFrom(it))
            genres.addAll(Synopsis.ThemesAndDemographics.extractFrom(it))
            if (removeBecauseGenres(genres)) return@forEach
            builder.setGenres(genres)

            val (type, startYear, status, eps, epLength) = Infos.extractFrom(it)
            builder.setAnimeType(type)
            builder.setStartYear(startYear)
            builder.setStatus(status)
            if (removeBecauseMinEps(eps)) return@forEach
            builder.setEpisodes(eps)
            builder.setEpisodeLength(epLength)

            val imgUrl: String = ImageUrl.extractFrom(it)
            builder.setImageURL(imgUrl)

            result.add(builder.build())
            builder.reset()
        }

    /* (contains)
	 * if user entered a name, filter out all anime that don't contain that name (ignore case)
	 */
    private fun removeBecauseName(animeName: String): Boolean =
        name != null && !animeName.contains(name!!, true)

    /* (and)
	 * (pretty sure this takes O(n^2) time, yikes)
	 * Remove all anime that don't contain all genres the user selected
	 */
    private fun removeBecauseGenres(genreSet: EnumSet<Genre>): Boolean =
        !genreSet.containsAll(genres)

    /* (or)
	 * if user has selected seasons, filter out all anime whose season
	 * is not one of those seasons
	 */
    // TODO: removeBecauseStartYear
    // TODO: removeBecauseAnimeType
    // TODO: removeBecauseStatus
    // not gonna filter episode length
    private fun removeBecauseSeason(s: Season?): Boolean {
        // user hasn't selected any Seasons
        if (seasons.isEmpty())
            return false

        // user selected at least 1 Season so filter all with no/UNDEF studio
        if (s == null || s === Season.UNDEF)
            return true

        return !seasons.contains(s)
    }

    /* (contains)
	 * if user entered a Studio, filter out all anime whose studios
	 * doesn't contain that name (ignore case)
	 */
    private fun removeBecauseStudio(s: Set<String>): Boolean {
        // user hasn't entered a Studio
        if (studio.isNullOrBlank())
            return false

        // user entered a Studio so remove all with no Studio
        if (s.isEmpty())
            return true

        // basically we 'want to remove' it until it has a 'valid' studio
        return !s.any { it.contains(this.studio!!, ignoreCase = true) }
    }

    private fun removeBecauseMinEps(eps: Int): Boolean {
        return if (minEpisodes == null || eps == Anime.NOT_FINISHED) false else eps < minEpisodes!!
    }
}
