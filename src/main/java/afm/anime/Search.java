package afm.anime;

//import static afm.utils.Utils.inJar;

import static afm.utils.Utils.inJar;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.net.ssl.SSLHandshakeException;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import afm.Main;
import afm.anime.Anime.AnimeBuilder;
import afm.utils.Utils;

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

	private static final String MAL_URL = "https://myanimelist.net";
	private static final String GENRE_URL = MAL_URL + "/anime/genre/"; //+genre.getId()


	/* css query Strings to get respective element from myanimelist.net */
	private static final String LINK_TITLE = "a.link-title",
								GENRE = "div.genres-inner.js-genre-inner",
								SRCS = "div.prodsrc",
								INFOS = "div.synopsis.js-synopsis",
								DATES = "span.remain-time",
								IMG_URLS = "img[data-src]";


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
				doc = Jsoup.connect(Search.GENRE_URL + genre.getId() + "/?page="+page).get();
				scrapeDocument(doc);
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
				if (!inJar()) e.printStackTrace();
				return false;
			}
		}

		return true;
	}

	// Scrapes the document, adding all appropriate anime to result
	private void scrapeDocument(Document doc) {
		Elements linkTitles = doc.select(Search.LINK_TITLE);

		// 1 line each genre list, delimiter is space " "
		Elements genres = doc.select(Search.GENRE);

		// studio - episodes("x eps") - source(manga/light novel/etc.)
		Elements srcs = doc.select(Search.SRCS);

		// 1 line of entire synopsis
		Elements infos = doc.select(Search.INFOS);

		// need to parseSeasonFromMALDate
		Elements dates = doc.select(Search.DATES);

		// need to do .absUrl("data-src") to get url
		Elements imgURLs = doc.select(Search.IMG_URLS);

		Iterator<Element> itL = linkTitles.iterator(),
						  itG = genres.iterator(),
						  itS = srcs.iterator(),
						  itI = infos.iterator(),
						  itD = dates.iterator(),
						  itIMGS = imgURLs.iterator();

		// Use one for memory lol
		AnimeBuilder builder = Anime.builder();

		while (itL.hasNext() && itG.hasNext() && itS.hasNext() && itI.hasNext() &&
			   itD.hasNext() && itIMGS.hasNext()) {

			builder.clear();

			Element nameElem = itL.next();

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
			int id = getIdFromURl(nameElem.attr("abs:href"));
			builder.setId(id);

			String[] genreArray = itG.next().text().split(" ");
			final EnumSet<Genre> genreSet;
			if (genreArray.length == 1 && genreArray[0].isEmpty()) // anime with no normal genre, only demographic/theme
				genreSet = EnumSet.noneOf(Genre.class);
			else
				genreSet = getGenreSet(genreArray);

			Element synopsisElement = itI.next();
			String synopsis = synopsisElement.selectFirst("span").text();
			builder.setInfo(synopsis);

			addThemesAndDemos(genreSet, synopsisElement);
			if (removeBecauseGenres(genreSet)) {
				itS.next();
				itD.next();
				itIMGS.next();
				continue;
			}
			builder.setGenres(genreSet);


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

			Integer episodes = null;
			for (i = sources.length - 1; i >= 0; i--) {
				String eps = sources[i];
				if (Utils.isNumeric(eps)) {
					episodes = Integer.parseInt(eps);
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
			result.add(builder.build());
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
		return name!= null && !animeName.toLowerCase().contains(name.toLowerCase());
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
		minEps = n;
		if (minEps > 72) minEps = 72;
	}
}
