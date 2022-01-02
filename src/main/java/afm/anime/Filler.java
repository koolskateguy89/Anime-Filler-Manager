package afm.anime;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import afm.utils.Utils;

record Filler(int start, int end) implements Comparable<Filler> {

	public boolean contains(int n) {
		return start <= n && n <= end;
	}

	@Override
	public String toString() {
		return (start == end) ? Integer.toString(end) : start + "-" + end;
	}

	// This smaller -> negative result
	@Override
	public int compareTo(Filler other) {
		return (start != other.start) ? start - other.start : end - other.end;
	}


	/* Start ,  End  , Object */
	private static final Table<Integer, Integer, Filler> CACHE = HashBasedTable.create();

	// For one episode filler
	public static Filler of(int episode) {
		return of(episode, episode);
	}

	public static Filler of(int start, int end) {
		Filler cached = CACHE.get(start, end);

		if (cached == null) {
			cached = new Filler(start, end);
			CACHE.put(start, end, cached);
		}

		return cached;
	}

	static Filler parseFiller(String s) {
		int divPos = s.indexOf('-');

		// single episode filler
		if (divPos == -1) {
			return Filler.of(Integer.parseInt(s));
		}

		int start = Integer.parseInt(s.substring(0, divPos));
		int end = Integer.parseInt(s.substring(divPos + 1));

		return Filler.of(start, end);
	}

	static List<Filler> getFillers(String name) {
		try {
			// replace all non-alphanumeric characters with a dash (which is what AFL does)
			String formattedName = formatName(name);

			Document doc = Jsoup.connect("https://www.animefillerlist.com/shows/" + formattedName).get();

			// the filler element is always the last episode element
			Elements episodeElements = doc.select("span.episodes");

			// the anime has no filler
			if (episodeElements.isEmpty())
				return Collections.emptyList();

			String[] fillerStrings = episodeElements.last().text().split(", ");

			return Arrays.stream(fillerStrings).map(Filler::parseFiller).toList();
		} catch (IOException io) {
			// the page doesn't exist, most likely the MAL name is different to the AFL name
			return Collections.emptyList();
		}
	}

	private static String formatName(String name) {
		// replace all non-alphanumeric characters with a dash (which is what AFL does)
		String formattedName = replaceNonAlphaNumericWithDash(name.toLowerCase());
		// name.toLowerCase().replaceAll("[^a-zA-Z0-9]+", "-")

		// get rid of leading/trailing dashes
		while (formattedName.length() > 1 && formattedName.charAt(0) == '-')
			formattedName = formattedName.substring(1);
		while (formattedName.length() > 1 && formattedName.charAt(formattedName.length() - 1) == '-')
			formattedName = formattedName.substring(0, formattedName.length() - 1);

		// basically if name includes a year, remove it
		int len = formattedName.length();
		if (len > 6 && Utils.isNumeric(formattedName.substring(len - 4))) {
			formattedName = formattedName.substring(0, len - 4);

			while (formattedName.length() > 1 && formattedName.charAt(0) == '-')
				formattedName = formattedName.substring(1);
			while (formattedName.length() > 1 && formattedName.charAt(formattedName.length() - 1) == '-')
				formattedName = formattedName.substring(0, formattedName.length() - 1);
		}

		return formattedName;
	}

	// this took avg ~600ns vs regex ~6-7k ns
	private static String replaceNonAlphaNumericWithDash(String s) {
		StringBuilder sb = new StringBuilder();

		// help with if multiple characters in a row are non-alphanumeric
		boolean lastWasNonAlpha = false;

		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			if (Character.isLetterOrDigit(ch)) {
				sb.append(ch);
				lastWasNonAlpha = false;
			} else if (!lastWasNonAlpha) {
				sb.append('-');
				lastWasNonAlpha = true;
			}
		}

		return sb.toString();
	}

}
