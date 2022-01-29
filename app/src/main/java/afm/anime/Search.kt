package afm.anime

import afm.Main
import afm.common.utils.currentYear
import afm.common.utils.emptyEnumSet
import afm.common.utils.inJar
import afm.common.utils.remove
import afm.common.utils.setAll
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
    private fun getIdFromUrl(url: String): Int {
        val start = 30 //where ID starts
        val end = url.indexOf('/', start + 1)
        return url.substring(start, end).toIntOrNull() ?: -1
    }

    override fun extractFrom(animeElem: Element): Pair<String, Int> {
        val nameElem: Element = animeElem.selectFirst(cssQuery)!!

        val name = nameElem.text()
        val url = nameElem.absUrl("href")
        val id = getIdFromUrl(url)

        return name to id
    }
}

private object Genres : Selector {
    override fun extractFrom(animeElem: Element): EnumSet<Genre> {
        val ids = animeElem.attr("data-genre").split(",")

        return ids.mapTo(emptyEnumSet()) {
            Genre.valueOfFromId(it.toInt())
        }
    }
}

private object Synopsis : Selector {
    private const val cssQuery = "div.synopsis.js-synopsis"

    private lateinit var synopsisElem: Element

    override fun extractFrom(animeElem: Element): String {
        synopsisElem = animeElem.selectFirst(cssQuery)!!
        return synopsisElem.selectFirst("p")!!.text()
    }

    object Studios : Selector {
        private const val cssQuery = "div.properties > div:nth-child(1) > span.item > a"
        // they should be unique anyway but yeah
        override fun extractFrom(animeElem: Element): Set<String> {
            val studioElems = synopsisElem.select(cssQuery)
            val studios = studioElems.map { it.text() }.toSet()
            return studios.ifEmpty { setOf("Unknown") }
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
            val names = synopsisElem.select(cssQuery)
            return names.mapTo(emptyEnumSet()) {
                Genre.valueOfFromName(it.text().remove(" "))
            }
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
 *  - if result contains less than 13 anime, search the next page
 *
 *  The removeBecauseX methods in this are in order of how they are used.
 */
class Search {
    /* search filters */
    var name: String? = null
    var studio: String? = null
    val genres: EnumSet<Genre> = emptyEnumSet()
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

    private val builder = Anime.builder()
    private val result = hashSetOf<Anime>()

    fun setGenres(genres: Collection<Genre>) {
        this.genres.setAll(genres)
    }

    fun search(): List<Anime> {
        /* this should be impossible because it's already taken care of
		   in SearchScreen */
        check(!genres.isEmpty()) { "No genres selected for searching" }

        tailrec fun search0(): List<Anime> {
            val searchWorked: Boolean = searchForEachGenre()

            if (!searchWorked || reachedLastPage || result.size >= 13 /*&& (name != null)*/) {
                // fail fast is search didn't work
                return result.sortedWith(Anime.SORT_BY_NAME)
            }

            page++
            return search0()
        }

        return search0()
    }

    // (and)
    // searches for each and every genre the user selected
    // returns if search 'worked'
    private fun searchForEachGenre(): Boolean {
        for (genre in genres) {
            try {
                scrapeDocument(Jsoup.connect("$GENRE_URL/${genre.id}/page=$page")
                    //.cookie("search_view", "list") // use the list search view to be able to get start date
                    .timeout(TIMEOUT_MILLIS)
                    .get()
                )
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
    private fun scrapeDocument(doc: Document) = doc.select(ANIME_ELEMS).forEach {
        // note: return@forEach == continue
        with(builder) {
            reset()

            val (name, id) = NameAndId.extractFrom(it)
            if (removeBecauseName(name)) return@forEach
            setName(name)
            setId(id)

            val synopsis: String = Synopsis.extractFrom(it)
            setSynopsis(synopsis)

            val studios: Set<String> = Synopsis.Studios.extractFrom(it)
            if (removeBecauseStudio(studios)) return@forEach
            setStudios(studios)

            val genres = emptyEnumSet<Genre>()
            genres.addAll(Genres.extractFrom(it))
            genres.addAll(Synopsis.ThemesAndDemographics.extractFrom(it))
            if (removeBecauseGenres(genres)) return@forEach
            setGenres(genres)

            val (type, startYear, status, eps, epLength) = Infos.extractFrom(it)

            if (removeBecauseAnimeType(type)) return@forEach
            setAnimeType(type)

            if (removeBecauseStartYear(startYear)) return@forEach
            setStartYear(startYear)

            if (removeBecauseStatus(status)) return@forEach
            setStatus(status)

            if (removeBecauseMinEps(eps)) return@forEach
            setEpisodes(eps)

            setEpisodeLength(epLength)

            val imgUrl: String = ImageUrl.extractFrom(it)
            setImageURL(imgUrl)

            result.add(build())
            reset()
        }
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

    /* (contains)
	 * if user entered a Studio, remove all anime whose studios
	 * don't contain that name (ignore case)
	 */
    private fun removeBecauseStudio(s: Set<String>): Boolean {
        // user hasn't entered a Studio
        if (studio.isNullOrBlank())
            return false

        // user entered a Studio so remove all with no Studio
        if (s.isEmpty())
            return true

        // basically we 'want to remove' the anime until it has a 'valid' studio
        return !s.any { it.contains(this.studio!!, ignoreCase = true) }
    }

    private fun removeBecauseAnimeType(type: AnimeType): Boolean =
        animeType != null && type != animeType

    private fun removeBecauseStartYear(year: Int): Boolean =
        startYear != null && year < startYear!!

    private fun removeBecauseStatus(s: Status) : Boolean =
        status != null && s != status

    private fun removeBecauseMinEps(eps: Int): Boolean =
        if (minEpisodes == null || eps == Anime.NOT_FINISHED)
            false
        else
            eps < minEpisodes!!
}
