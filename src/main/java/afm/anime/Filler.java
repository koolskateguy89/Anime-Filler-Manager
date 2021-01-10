package afm.anime;

import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import afm.utils.Utils;

// AFL = Anime Filler List
@SuppressWarnings("preview")
public record Filler(int start, int end) implements Comparable<Filler> {
	
	static void addFillersTo(Anime anime) {
		/* don't search for filler for any custom anime
		 * (Anime already checks for this but check again in case) */
		if (anime.isCustom())
			return;
		
		try {
			// replace all non-alphanumeric characters with a dash (which is what AFL does)
			String formattedName = anime.getName().toLowerCase().replaceAll("[^a-zA-Z0-9]+", "-");
			
			// get rid of leading/trailing dashes
			while (formattedName.length() > 1 && formattedName.charAt(0) == '-')
				formattedName = formattedName.substring(1);
			while (formattedName.length() > 1 && formattedName.charAt(formattedName.length()-1) == '-')
				formattedName = formattedName.substring(0, formattedName.length()-1);
			
			// basically if name includes a year, remove it
			int len = formattedName.length();
			if (len > 6 && Utils.isNumeric(formattedName.substring(len - 4))) {
				formattedName = formattedName.substring(0, len-4);
				
				while (formattedName.length() > 1 && formattedName.charAt(0) == '-')
					formattedName = formattedName.substring(1);
				while (formattedName.length() > 1 && formattedName.charAt(formattedName.length()-1) == '-')
					formattedName = formattedName.substring(0, formattedName.length()-1);
			}
			
			Document doc = Jsoup.connect("https://www.animefillerlist.com/shows/" + formattedName).get();
			
			// the filler element is always the last episode element
			Elements episodeElements = doc.select("span.episodes");
			
			// the anime has no filler
			if (episodeElements.isEmpty())
				return;
			
			String[] fillerStrings = episodeElements.last().text().split(", ");
			
			for (String f : fillerStrings) {
				anime.addFiller(parseFiller(f));
			}
		} catch (IOException e) {
			//e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}
	
	static Filler parseFiller(String s) {
		int divPos = s.indexOf('-');
		
		// single episode filler
		if (divPos == -1) {
			return new Filler(Integer.parseInt(s));
		}
		
		int start = Integer.parseInt(s.substring(0, divPos));
		int end = Integer.parseInt(s.substring(divPos+1));
		
		return new Filler(start, end);
	}
	
	
	// for single episode filler
	private Filler(int episode) {
		this(episode, episode);
	}
	
	public int length() {
		return end - start + 1;
	}
	
	@Override public String toString() {
		return (end - start == 0) ? Integer.toString(end) : start + "-" + end;
	}

	// This smaller -> negative result
	@Override public int compareTo(Filler o) {
		return (start != o.start) ? start - o.start : end - o.end;
	}
	
	@Override public boolean equals(Object o) {
		if (o == null)
			return false;
		
		if (this == o)
			return true;
		
		if (o instanceof Filler other) {
			return start == other.start && end == other.end;
		}
		return false;
	}
	
	@Override public int hashCode() {
		int result = 1;
		result = 31 * result + start;
		result = 31 * result + end;
		return result;
	}
}
