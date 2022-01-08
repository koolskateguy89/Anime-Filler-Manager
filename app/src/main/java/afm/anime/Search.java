package afm.anime;

import static afm.common.Utils.inJar;
import static java.util.Objects.requireNonNullElseGet;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.net.ssl.SSLHandshakeException;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import kotlin.StandardKt;

import org.checkerframework.checker.units.qual.A;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import afm.Main;
import afm.anime.Anime.AnimeBuilder;
import afm.common.Utils;

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
public class Search {

	/* search filters */
	private String name;
	private String studio;
	private final EnumSet<Genre> genres = EnumSet.noneOf(Genre.class);
	private final HashSet<Season> seasons = new HashSet<>();
	private Integer minEps = null;


	/* class variables to assist web scraping */
	private int page = 1;
	private boolean reachedLastPage = false;

	/* not using LinkedHS/TreeSet as it's going to be sorted anyway */
	/* also we don't want duplicate anime */
	private final HashSet<Anime> result = new HashSet<>();
	private static final AnimeBuilder builder = Anime.builder();

	private static final String MAL_URL = "https://myanimelist.net";
	private static final String GENRE_URL = MAL_URL + "/anime/genre/"; //+genre.getId()


	/* css query Strings to get respective element from myanimelist.net */
	private static final String LINK_TITLE = "a.link-title",
								GENRE = "div.genres-inner.js-genre-inner",
								SRCS = "div.prodsrc",
								INFOS = "div.synopsis.js-synopsis",
								DATES = "span.remain-time", // FIXME: dates no elems
								IMG_URLS = "img[data-src]",
								PROPERTIES = "div.synopsis.js-synopsis > div";

	// #content > div.js-categories-seasonal.js-block-list.tile.mt16 > div > div
	// https://jsoup.org/cookbook/extracting-data/selector-syntax
	private static final String
			ANIME = "#content > div.js-categories-seasonal.js-block-list.tile.mt16 > div > div.seasonal-anime.js-seasonal-anime",
			// TODO
			NAME = "div:nth-child(1) > div.title > div.title-text > h2.h2_anime_title > a",
			// TODO
			IMG = "div.image > a:nth-child(1) > img",
			// TODO
			LINK_TTLE = "div:nth-child(1) > div.title > div > h2 > a"
	;

	private static class Selectors {
		static class Anime {

		}
	}

	private static final int TIMEOUT_MILLIS = 8000;


	public List<Anime> search() {
		/* this should be impossible because it's already taken care of
		   in SearchScreen */
		if (genres.isEmpty())
			throw new Error(new IllegalArgumentException("No genres selected for searching"));

		boolean searchWorked = searchForEachGenre();

		if (searchWorked && !reachedLastPage && result.size() < 13 /*&& (name == null)*/) {
			page++;
			/* I don't want it to continue as other search() calls
			 * will sort - which only need to happen once */
			return search();
		}

		// Cannot sort a HashSet so wrap it in a List and sort
		ArrayList<Anime> resultList = new ArrayList<>(result);
		resultList.sort(Anime.SORT_BY_NAME);

		return resultList;
	}

	// (and)
	// searches for each and every genre the user selected
	// returns if search 'worked'
	private boolean searchForEachGenre() {
		for (Genre genre : genres) {
			final Document doc;
			try {
				doc = Jsoup.connect(Search.GENRE_URL + genre.getId() + "/?page="+page)
						.userAgent(HttpConnection.DEFAULT_UA)
						//.cookie("search_view", "list") // use the list search view to be able to get start date
						.timeout(TIMEOUT_MILLIS)
						.get();
				//System.out.println(doc.outerHtml());
				scrapeDocumentNew(doc);
				//scrapeDocument(doc);
			} catch (UnknownHostException uhe) {
				// no internet connection
				Platform.runLater(() -> {
					Alert a = new Alert(AlertType.ERROR, "No internet connection");
					a.initOwner(Main.getStage());
					a.showAndWait();
				});
				return false;
			} catch (HttpStatusException htse) {
				// gone past last page
				reachedLastPage = true;
			} catch (SSLHandshakeException she) {
				// most likely caused by MAL being blocked or something
				Platform.runLater(() -> {
					Alert a = new Alert(AlertType.ERROR, "Could not connect to MyAnimeList");
					a.initOwner(Main.getStage());
					a.showAndWait();
				});
				return false;
			} catch (IOException e) {
				if (!inJar())
					e.printStackTrace();
				return false;
			}
		}

		return true;
	}

	private void scrapeDocumentNew(final Document doc) {
		Elements animeElems = doc.select(Search.ANIME);
		animeElems.trimToSize();

		animeElems.forEach(this::buildAnimeFromElement);

		builder.clear();
	}

	private void buildAnimeFromElement(Element animeElem) {
		builder.clear();

		String name = animeElem.selectFirst(Search.NAME).text();



		if (removeBecauseName(name))
			return;
		builder.setName(name);
		//System.out.println(" " + name);

		if (true) return;

		// TODO: id


		var genres = getGenres(animeElem);
		if (removeBecauseGenres(genres))
			return;
		builder.setGenres(genres);
	}

	private static String getName(Element animeElem) {
		//noinspection ConstantConditions
		return animeElem.selectFirst(Search.NAME).text();
	}

	private static int getId(Element animeElem) {

		return 0;
	}

	private static EnumSet<Genre> getGenres(Element animeElem) {
		String[] ids = animeElem.attr("data-genre").split(",");

		EnumSet<Genre> result = EnumSet.noneOf(Genre.class);
		for (String id : ids)
			result.add(Genre.valueOfFromName(id));
		return result;
	}

	private static String getSynopsis(Element animeElem) {
		return null;
	}

	private static String getImageUrl(Element animeElem) {
		return null;
	}

	// Scrapes the document, adding all appropriate anime to result
	private void scrapeDocument(Document doc) {
		Elements linkTitles = doc.select(Search.LINK_TITLE);
		System.out.print(linkTitles.size());

		// 1 line each genre list, delimiter is space " "
		Elements genres = doc.select(Search.GENRE);
		System.out.print(" " + genres.size());

		// studio - episodes("x eps") - source(manga/light novel/etc.)
		Elements srcs = doc.select(Search.SRCS);
		System.out.print(" " + srcs.size());

		// 1 line of entire synopsis
		Elements infos = doc.select(Search.INFOS);
		System.out.print(" " + infos.size());

		// need to parseSeasonFromMALDate
		// FIXME:
		Elements dates = doc.select(Search.DATES);
		System.out.print(" " + dates.size());

		// need to do .absUrl("data-src") to get url
		Elements imgURLs = doc.select(Search.IMG_URLS);
		System.out.println(" " + imgURLs.size());
		System.out.println(srcs.text());
		System.out.println();

		Iterator<Element> itL = linkTitles.iterator(),
						  itG = genres.iterator(),
						  itS = srcs.iterator(),
						  itI = infos.iterator(),
						  itD = dates.iterator(),
						  itIMGS = imgURLs.iterator();

		while (itL.hasNext() && itG.hasNext() && itS.hasNext() && itI.hasNext() &&
			   itD.hasNext() && itIMGS.hasNext()) {

			builder.clear();

			Element nameElem = itL.next();

			System.out.print("name");
			// MyAnimeList spells [Naruto: Shippuden] incorrectly
			String animeName = nameElem.text().replace("Shippuuden", "Shippuden").trim();

			if (removeBecauseName(animeName)) {
				itG.next();
				itI.next();
				itS.next();
				itD.next();
				itIMGS.next();
				continue;
			}

			builder.setName(animeName);

			// get the anime's ID
			System.out.print(" id");
			int id = getIdFromURl(nameElem.attr("abs:href"));
			builder.setId(id);

			System.out.print(" genre");
			String[] genreArray = itG.next().text().split(" ");
			final EnumSet<Genre> genreSet;
			if (genreArray.length == 1 && genreArray[0].isEmpty()) // anime with no normal genre, only demographic/theme
				genreSet = EnumSet.noneOf(Genre.class);
			else
				genreSet = getGenreSet(genreArray);

			System.out.print(" syno");
			Element synopsisElement = itI.next();
			String synopsis = synopsisElement.selectFirst("span").text();
			builder.setInfo(synopsis);

			System.out.print(" the & demo");
			addThemesAndDemos(genreSet, synopsisElement);
			if (removeBecauseGenres(genreSet)) {
				itS.next();
				itD.next();
				itIMGS.next();
				continue;
			}
			builder.setGenres(genreSet);

			System.out.print(" sources (studio)");
			String[] sources = itS.next().text().split(" ");
			StringBuilder sb = new StringBuilder(sources[0]);
			int i = 1;
			// For studios with names that take up multiple slots in sources
			while (i < sources.length-1 && ((!Utils.isNumeric(sources[i]) && !sources[i].equals("?")) ||
					sb.toString().equals("Studio"))) {
				sb.append(' ').append(sources[i]);
				i++;
			}
			String animeStudio = sb.toString();
			if (removeBecauseStudio(animeStudio)) {
				itD.next();
				itIMGS.next();
				continue;
			}
			builder.setStudio(animeStudio);

			System.out.print(" episodes (minEps)");
			Integer episodes = null;
			for (i = sources.length - 1; i >= 0; i--) {
				String eps = sources[i];
				Integer e = Utils.toIntOrNull(eps);
				if (e != null) {
					episodes = e;
					break;
				} else if (eps.equals("?")) {
					episodes = Anime.NOT_FINISHED;
					break;
				}
			}
			// (it checks for null: if not null, don't remove)
			if (removeBecauseMinEps(episodes)) {
				itD.next();
				itIMGS.next();
				continue;
			}
			if (episodes == null)
				episodes = Anime.NOT_FINISHED;
			builder.setEpisodes(episodes);


			System.out.print(" season");
			Season season = Season.parseSeasonFromMALDate(itD.next().text());
			if (removeBecauseSeason(season)) {
				itIMGS.next();
				continue;
			}
			builder.setSeason(season);


			try {
				String imgURL = itIMGS.next().absUrl("data-src");
				builder.setImageURL(imgURL);
			} catch (IllegalArgumentException | NullPointerException ignored) {
				// no need to retry
			}

			// building the anime will find its fillers
			System.out.println(" build");
			result.add(builder.build());
			System.out.println();
		}
	}

	/*
	 * Link will be in form:
	 * https://myanimelist.net/anime/[ID]/[TITLE]
	 */
	private static int getIdFromURl(String url) {
		int start = 30;	//where ID starts
		int end = url.indexOf('/', start+1);

		try {
			return Integer.parseInt(url.substring(start, end));
		} catch (NumberFormatException nfe) {
			return -1;
		}
	}

	// return the genres of the anime - from genreList (from MAL website)
	// (this effectively parses the data from MAL website to a Genre set)
	private static EnumSet<Genre> getGenreSet(String[] genreList) {
		EnumSet<Genre> genreSet = EnumSet.noneOf(Genre.class);

		for (int i = 0; i < genreList.length; i++) {
			String genreWord = genreList[i];
			String genreName = null;

			if (genreWord.equals("Sci-Fi")) {
				genreName = "SciFi";
			} else if (i < genreList.length - 1) {
				String next = genreList[i+1];
				if (isTwoWordGenre(genreWord, next)) {
					genreName = genreWord + next;
					i++;
				}
				// Slice Of Life
				else if (i < genreList.length - 2 && genreWord.equals("Slice")) {
					genreName = "SliceOfLife";
					i += 2;
				}
			}

			// 'normal' genre (1 word)
			if (genreName == null)
				genreName = genreList[i];

			genreSet.add(Genre.valueOfFromName(genreName));
		}

		return genreSet;
	}

	private static boolean isTwoWordGenre(String genreWord, String next) {
		return genreWord.equals("Avant") || genreWord.equals("Award") || genreWord.equals("Martial") ||
				genreWord.equals("Super") || next.equals("Life") || next.equals("Love");
	}

	/*
	 * Examples of lines:
	 *   Themes: Mecha, Space, Super Power
	 *   Theme: Samurai
	 *   Demographic: Shounen
	 *   Demographics: Josei, Shoujo
	 * Multiple demographics is somewhat rare
	 */
	private static void addThemesAndDemos(EnumSet<Genre> genreSet, Element synopsisElem) {
		synopsisElem.select("p").forEach(elem -> elem.text().lines().forEach(line -> {
			if (line.startsWith("Demographic") || line.startsWith("Theme")) {
				String strings = line.substring(line.indexOf(':') + 2);

				for (String s : strings.split(", "))
					genreSet.add(Genre.valueOfFromName(s.replace(" ", "")));
			}
		}));
	}


	/* (contains)
	 * if user entered a name, filter out all anime that don't contain that name (ignore case)
	 */
	private boolean removeBecauseName(String animeName) {
		return name != null && !animeName.toLowerCase().contains(name.toLowerCase());
	}

	/* (and)
	 * (pretty sure this takes O(n^2) time, yikes)
	 * Remove all anime that don't contain all genres the user selected
	 */
	private boolean removeBecauseGenres(EnumSet<Genre> genreSet) {
		return !genreSet.containsAll(this.genres);
	}

	/* (or)
	 * if user has selected seasons, filter out all anime whose season
	 * is not one of those seasons
	 */
	private boolean removeBecauseSeason(Season s) {
		// user hasn't selected any Seasons
		if (seasons.isEmpty())
			return false;

		// user selected at least 1 Season so filter all with no/UNDEF studio
		if (s == null || s == Season.UNDEF)
			return true;

		return !seasons.contains(s);
	}

	/* (contains)
	 * if user entered a Studio, filter out all anime whose Studio
	 * doesn't contain that name (ignore case)
	 */
	private boolean removeBecauseStudio(String s) {
		// user hasn't entered a Studio
		if (studio == null || studio.isBlank())
			return false;

		// user entered a Studio so filter all with no Studio
		if (s == null || s.isBlank())
			return true;

		return !s.toLowerCase().contains(this.studio.toLowerCase());
	}


	private boolean removeBecauseMinEps(Integer eps) {
		if (minEps == null || eps == null || eps == Anime.NOT_FINISHED)
			return false;
		else
			return eps < minEps;
	}

	public void setName(String n) {
		name = n;
	}

	public void setStudio(String s) {
		studio = s;
	}

	public void setGenres(Collection<Genre> genres) {
		this.genres.clear();
		this.genres.addAll(genres);
	}

	public void setSeasons(Set<Season> set) {
		seasons.clear();
		seasons.addAll(set);
	}

	public void setMinEpisodes(int n) {
		minEps = Math.min(n, 72);
	}
}
