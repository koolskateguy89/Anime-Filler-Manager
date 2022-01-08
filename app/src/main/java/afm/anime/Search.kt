package afm.anime

import afm.Main
import afm.anime.Genre.id
import afm.common.inJar
import afm.anime.Genre.Companion.valueOfFromName
import afm.common.isNumeric
import afm.common.toIntOrNull
import java.util.EnumSet
import afm.anime.Genre
import java.util.HashSet
import java.lang.IllegalArgumentException
import org.jsoup.Jsoup
import afm.anime.Search
import java.lang.Runnable
import javafx.scene.control.Alert.AlertType
import org.jsoup.HttpStatusException
import javax.net.ssl.SSLHandshakeException
import java.io.IOException
import java.lang.StringBuilder
import java.lang.NullPointerException
import java.util.Locale
import afm.anime.Anime.AnimeBuilder
import javafx.application.Platform
import javafx.scene.control.Alert
import org.jsoup.helper.HttpConnection
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.lang.Error
import java.lang.NumberFormatException
import java.net.UnknownHostException
import java.util.ArrayList
import java.util.function.Consumer

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
    private var name: String? = null
    private var studio: String? = null
    private val genres = EnumSet.noneOf(Genre::class.java)
    private val seasons = HashSet<Season>()
    private var minEps: Int? = null

    /* class variables to assist web scraping */
    private var page = 1
    private var reachedLastPage = false

    /* not using LinkedHS/TreeSet as it's going to be sorted anyway */ /* also we don't want duplicate anime */
    private val result = HashSet<Anime>()

    private class Selectors {
        internal class Anime
    }

    fun search(): List<Anime> {
        /* this should be impossible because it's already taken care of
		   in SearchScreen */
        if (genres.isEmpty()) throw Error(IllegalArgumentException("No genres selected for searching"))
        val searchWorked = searchForEachGenre()
        if (searchWorked && !reachedLastPage && result.size < 13 /*&& (name == null)*/) {
            page++
            /* I don't want it to continue as other search() calls
			 * will sort - which only need to happen once */return search()
        }

        // Cannot sort a HashSet so wrap it in a List and sort
        val resultList = ArrayList(result)
        resultList.sort(Anime.SORT_BY_NAME)
        return resultList
    }

    // (and)
    // searches for each and every genre the user selected
    // returns if search 'worked'
    private fun searchForEachGenre(): Boolean {
        for (genre in genres) {
            val doc: Document
            try {
                doc = Jsoup.connect(GENRE_URL + genre!!.id + "/?page=" + page)
                    .userAgent(HttpConnection.DEFAULT_UA) //.cookie("search_view", "list") // use the list search view to be able to get start date
                    .timeout(TIMEOUT_MILLIS)
                    .get()
                //System.out.println(doc.outerHtml());
                scrapeDocumentNew(doc)
                //scrapeDocument(doc);
            } catch (uhe: UnknownHostException) {
                // no internet connection
                Platform.runLater {
                    val a = Alert(AlertType.ERROR, "No internet connection")
                    a.initOwner(Main.getStage())
                    a.showAndWait()
                }
                return false
            } catch (htse: HttpStatusException) {
                // gone past last page
                reachedLastPage = true
            } catch (she: SSLHandshakeException) {
                // most likely caused by MAL being blocked or something
                Platform.runLater {
                    val a = Alert(AlertType.ERROR, "Could not connect to MyAnimeList")
                    a.initOwner(Main.getStage())
                    a.showAndWait()
                }
                return false
            } catch (e: IOException) {
                if (!inJar) e.printStackTrace()
                return false
            }
        }
        return true
    }

    private fun scrapeDocumentNew(doc: Document) {
        val animeElems = doc.select(ANIME)
        animeElems.trimToSize()
        animeElems.forEach(Consumer { animeElem: Element -> buildAnimeFromElement(animeElem) })
        builder.clear()
    }

    private fun buildAnimeFromElement(animeElem: Element) {
        builder.clear()
        val name = animeElem.selectFirst(NAME)!!.text()
        if (removeBecauseName(name)) return
        builder.setName(name)
        //System.out.println(" " + name);
        if (true) return

        // TODO: id
        val genres = getGenres(animeElem)
        if (removeBecauseGenres(genres)) return
        builder.setGenres(genres)
    }

    // Scrapes the document, adding all appropriate anime to result
    private fun scrapeDocument(doc: Document) {
        val linkTitles = doc.select(LINK_TITLE)
        print(linkTitles.size)

        // 1 line each genre list, delimiter is space " "
        val genres = doc.select(GENRE)
        print(" " + genres.size)

        // studio - episodes("x eps") - source(manga/light novel/etc.)
        val srcs = doc.select(SRCS)
        print(" " + srcs.size)

        // 1 line of entire synopsis
        val infos = doc.select(INFOS)
        print(" " + infos.size)

        // need to parseSeasonFromMALDate
        // FIXME:
        val dates = doc.select(DATES)
        print(" " + dates.size)

        // need to do .absUrl("data-src") to get url
        val imgURLs = doc.select(IMG_URLS)
        println(" " + imgURLs.size)
        println(srcs.text())
        println()
        val itL: Iterator<Element> = linkTitles.iterator()
        val itG: Iterator<Element> = genres.iterator()
        val itS: Iterator<Element> = srcs.iterator()
        val itI: Iterator<Element> = infos.iterator()
        val itD: Iterator<Element> = dates.iterator()
        val itIMGS: Iterator<Element> = imgURLs.iterator()
        while (itL.hasNext() && itG.hasNext() && itS.hasNext() && itI.hasNext() &&
            itD.hasNext() && itIMGS.hasNext()
        ) {
            builder.clear()
            val nameElem = itL.next()
            print("name")
            // MyAnimeList spells [Naruto: Shippuden] incorrectly
            val animeName = nameElem.text().replace("Shippuuden", "Shippuden").trim { it <= ' ' }
            if (removeBecauseName(animeName)) {
                itG.next()
                itI.next()
                itS.next()
                itD.next()
                itIMGS.next()
                continue
            }
            builder.setName(animeName)

            // get the anime's ID
            print(" id")
            val id = getIdFromURl(nameElem.attr("abs:href"))
            builder.setId(id)
            print(" genre")
            val genreArray = itG.next().text().split(" ".toRegex()).toTypedArray()
            val genreSet: EnumSet<Genre?>
            genreSet =
                if (genreArray.size == 1 && genreArray[0].isEmpty()) // anime with no normal genre, only demographic/theme
                    EnumSet.noneOf(Genre::class.java) else getGenreSet(genreArray)
            print(" syno")
            val synopsisElement = itI.next()
            val synopsis = synopsisElement.selectFirst("span")!!.text()
            builder.setInfo(synopsis)
            print(" the & demo")
            addThemesAndDemos(genreSet, synopsisElement)
            if (removeBecauseGenres(genreSet)) {
                itS.next()
                itD.next()
                itIMGS.next()
                continue
            }
            builder.setGenres(genreSet)
            print(" sources (studio)")
            val sources = itS.next().text().split(" ".toRegex()).toTypedArray()
            val sb = StringBuilder(sources[0])
            var i = 1
            // For studios with names that take up multiple slots in sources
            while (i < sources.size - 1 && (!sources[i].isNumeric() && sources[i] != "?" || sb.toString() == "Studio")) {
                sb.append(' ').append(sources[i])
                i++
            }
            val animeStudio = sb.toString()
            if (removeBecauseStudio(animeStudio)) {
                itD.next()
                itIMGS.next()
                continue
            }
            builder.setStudio(animeStudio)
            print(" episodes (minEps)")
            var episodes: Int? = null
            i = sources.size - 1
            while (i >= 0) {
                val eps = sources[i]
                val e = toIntOrNull(eps)
                if (e != null) {
                    episodes = e
                    break
                } else if (eps == "?") {
                    episodes = Anime.NOT_FINISHED
                    break
                }
                i--
            }
            // (it checks for null: if not null, don't remove)
            if (removeBecauseMinEps(episodes)) {
                itD.next()
                itIMGS.next()
                continue
            }
            if (episodes == null) episodes = Anime.NOT_FINISHED
            builder.setEpisodes(episodes)
            print(" season")
            val season = Season.parseSeasonFromMALDate(itD.next().text())
            if (removeBecauseSeason(season)) {
                itIMGS.next()
                continue
            }
            builder.setSeason(season)
            try {
                val imgURL = itIMGS.next().absUrl("data-src")
                builder.setImageURL(imgURL)
            } catch (ignored: IllegalArgumentException) {
                // no need to retry
            } catch (ignored: NullPointerException) {
            }

            // building the anime will find its fillers
            println(" build")
            result.add(builder.build())
            println()
        }
    }

    /* (contains)
	 * if user entered a name, filter out all anime that don't contain that name (ignore case)
	 */
    private fun removeBecauseName(animeName: String): Boolean {
        return name != null && !animeName.lowercase(Locale.getDefault()).contains(name!!.lowercase(Locale.getDefault()))
    }

    /* (and)
	 * (pretty sure this takes O(n^2) time, yikes)
	 * Remove all anime that don't contain all genres the user selected
	 */
    private fun removeBecauseGenres(genreSet: EnumSet<Genre?>): Boolean {
        return !genreSet.containsAll(genres)
    }

    /* (or)
	 * if user has selected seasons, filter out all anime whose season
	 * is not one of those seasons
	 */
    private fun removeBecauseSeason(s: Season?): Boolean {
        // user hasn't selected any Seasons
        if (seasons.isEmpty()) return false

        // user selected at least 1 Season so filter all with no/UNDEF studio
        return if (s == null || s === Season.UNDEF) true else !seasons.contains(s)
    }

    /* (contains)
	 * if user entered a Studio, filter out all anime whose Studio
	 * doesn't contain that name (ignore case)
	 */
    private fun removeBecauseStudio(s: String?): Boolean {
        // user hasn't entered a Studio
        if (studio == null || studio!!.isBlank()) return false

        // user entered a Studio so filter all with no Studio
        return if (s == null || s.isBlank()) true else !s.lowercase(Locale.getDefault())
            .contains(studio!!.lowercase(Locale.getDefault()))
    }

    private fun removeBecauseMinEps(eps: Int?): Boolean {
        return if (minEps == null || eps == null || eps == Anime.NOT_FINISHED) false else eps < minEps!!
    }

    fun setName(n: String?) {
        name = n
    }

    fun setStudio(s: String?) {
        studio = s
    }

    fun setGenres(genres: Collection<Genre?>?) {
        this.genres.clear()
        this.genres.addAll(genres!!)
    }

    fun setSeasons(set: Set<Season>?) {
        seasons.clear()
        seasons.addAll(set!!)
    }

    fun setMinEpisodes(n: Int) {
        minEps = Math.min(n, 72)
    }

    companion object {
        private val builder = Anime.builder()
        private const val MAL_URL = "https://myanimelist.net"
        private const val GENRE_URL = MAL_URL + "/anime/genre/" //+genre.getId()

        /* css query Strings to get respective element from myanimelist.net */
        private const val LINK_TITLE = "a.link-title"
        private const val GENRE = "div.genres-inner.js-genre-inner"
        private const val SRCS = "div.prodsrc"
        private const val INFOS = "div.synopsis.js-synopsis"
        private const val DATES = "span.remain-time"

        // FIXME: dates no elems
        private const val IMG_URLS = "img[data-src]"
        private const val PROPERTIES = "div.synopsis.js-synopsis > div"

        // #content > div.js-categories-seasonal.js-block-list.tile.mt16 > div > div
        // https://jsoup.org/cookbook/extracting-data/selector-syntax
        private const val ANIME =
            "#content > div.js-categories-seasonal.js-block-list.tile.mt16 > div > div.seasonal-anime.js-seasonal-anime"

        // TODO
        private const val NAME = "div:nth-child(1) > div.title > div.title-text > h2.h2_anime_title > a"

        // TODO
        private const val IMG = "div.image > a:nth-child(1) > img"

        // TODO
        private const val LINK_TTLE = "div:nth-child(1) > div.title > div > h2 > a"
        private const val TIMEOUT_MILLIS = 8000
        private fun getName(animeElem: Element): String {
            return animeElem.selectFirst(NAME)!!.text()
        }

        private fun getId(animeElem: Element): Int {
            return 0
        }

        private fun getGenres(animeElem: Element): EnumSet<Genre?> {
            val ids = animeElem.attr("data-genre").split(",".toRegex()).toTypedArray()
            val result = EnumSet.noneOf(Genre::class.java)
            for (id in ids) result.add(valueOfFromName(id))
            return result
        }

        private fun getSynopsis(animeElem: Element): String? {
            return null
        }

        private fun getImageUrl(animeElem: Element): String? {
            return null
        }

        /*
	 * Link will be in form:
	 * https://myanimelist.net/anime/[ID]/[TITLE]
	 */
        private fun getIdFromURl(url: String): Int {
            val start = 30 //where ID starts
            val end = url.indexOf('/', start + 1)
            return try {
                url.substring(start, end).toInt()
            } catch (nfe: NumberFormatException) {
                -1
            }
        }

        // return the genres of the anime - from genreList (from MAL website)
        // (this effectively parses the data from MAL website to a Genre set)
        private fun getGenreSet(genreList: Array<String>): EnumSet<Genre?> {
            val genreSet = EnumSet.noneOf(Genre::class.java)
            var i = 0
            while (i < genreList.size) {
                val genreWord = genreList[i]
                var genreName: String? = null
                if (genreWord == "Sci-Fi") {
                    genreName = "SciFi"
                } else if (i < genreList.size - 1) {
                    val next = genreList[i + 1]
                    if (isTwoWordGenre(genreWord, next)) {
                        genreName = genreWord + next
                        i++
                    } else if (i < genreList.size - 2 && genreWord == "Slice") {
                        genreName = "SliceOfLife"
                        i += 2
                    }
                }

                // 'normal' genre (1 word)
                if (genreName == null) genreName = genreList[i]
                genreSet.add(valueOfFromName(genreName))
                i++
            }
            return genreSet
        }

        private fun isTwoWordGenre(genreWord: String, next: String): Boolean {
            return genreWord == "Avant" || genreWord == "Award" || genreWord == "Martial" || genreWord == "Super" || next == "Life" || next == "Love"
        }

        /*
	 * Examples of lines:
	 *   Themes: Mecha, Space, Super Power
	 *   Theme: Samurai
	 *   Demographic: Shounen
	 *   Demographics: Josei, Shoujo
	 * Multiple demographics is somewhat rare
	 */
        private fun addThemesAndDemos(genreSet: EnumSet<Genre?>, synopsisElem: Element) {
            synopsisElem.select("p").forEach(Consumer { elem: Element ->
                elem.text().lines().forEach(
                    Consumer { line: String ->
                        if (line.startsWith("Demographic") || line.startsWith("Theme")) {
                            val strings = line.substring(line.indexOf(':') + 2)
                            for (s in strings.split(", ".toRegex())
                                .toTypedArray()) genreSet.add(valueOfFromName(s.replace(" ", "")))
                        }
                    })
            })
        }
    }
}
