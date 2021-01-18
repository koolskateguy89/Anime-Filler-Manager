package afm.anime;

import java.util.Objects;
import afm.utils.Utils;

// This is immutable
// could implement making it a Record instead
public final class Season implements Comparable<Season> {

	public static final int SPRING = 0, SUMMER = 1, FALL = 2, WINTER = 3;
	// [last 2 digits of year][season(spring/...)]
	private static final Season[][] seasons = new Season[100][4];

	// For edge case anime that have "??? ??, ????" as their date
	public static final Season UNDEF = new Season("undef", -1);

	public static void init() {
		// Loop through the years (1999 -> current year) and generate all seasons for that year
		int szn;
		for (int year = 1999; year <= Utils.getCurrentYear(); year++) {
			szn = Season.SPRING;
			while (szn <= Season.WINTER) {
				seasons[year % 100][szn] = new Season(getSeasonFromInt(szn), year);
				szn++;
			}
		}
	}

	/***********************************************
	 * 											   *
	 * 				Static methods				   *
	 * 											   *
	 ***********************************************/

	// not used atm >:(
	public static Season[] values() {
		// Size should be equal to number of years * number of seasons
		Season[] values = new Season[(Utils.getCurrentYear()-1999+1)*4];
		int i = 0;
		int szn;
		for (int year = 1999; i < values.length && year <= Utils.getCurrentYear(); year++) {
			szn = Season.SPRING;
			while (szn <= Season.WINTER) {
				values[i] = seasons[year % 100][szn];
				szn++;
				i++;
			}
		}

		return values;
	}

	// return the String for a season given its int
	private static String getSeasonFromInt(int num) {
		return switch (num) {
			case SPRING -> "Spring";
			case SUMMER -> "Summer";
			case FALL -> "Fall";
			default -> "Winter";
		};
	}

	// returns a Season given its toString() output as input
	public static Season getSeasonFromToString(String str) {
		if (str == null)
			return null;

		if (str.equals("-"))
			return UNDEF;

		int spacePos = str.indexOf(' ');
		int year = Integer.parseInt(str.substring(spacePos+1));
		return getSeason(str.substring(0, spacePos), year);
	}

	// helper for sorting and parsing
	private static int getSeasonInt(String season) {
		return switch (season) {
			case "Spring" -> SPRING;
			case "Summer" -> SUMMER;
			case "Fall" -> FALL;
			default -> WINTER;
		};
	}

	// returns a Season given the season(String e.g. Winter) and year
	// used in search & custom screen
	public static Season getSeason(String season, int year) {
		try {
			return seasons[year % 100][Season.getSeasonInt(season)];
		} catch (IndexOutOfBoundsException e) {
			return UNDEF;
		}
	}

	public static Season getSeason(int szn, int year) {
		try {
			return seasons[year % 100][szn];
		} catch (IndexOutOfBoundsException e) {
			return UNDEF;
		}
	}

	/** Helper for parsing web scraped data, see {@link #parseSeasonFromMALDate(String)
	 * parseSeasonFromMALDate} below
	 */
	private static int getSznFromMonth(String month) {
		return
				switch (month) {
					case "Dec", "Jan", "Feb" -> WINTER;
					case "Mar", "Apr", "May" -> SPRING;
					case "Jun", "Jul", "Aug" -> SUMMER;
					default -> FALL;
				};
	}

	public static Season parseSeasonFromMALDate(String date) {
		if (date.contains("?"))
			return UNDEF;

		// Dates are split by spaces and commas e.g. "Jan 10, 2015, 00:55 (JST)"
		String[] values = date.split(" ");

		int szn = getSznFromMonth(values[0]);
		int yr = Integer.parseInt(values[2].replace(",", "")) % 100;

		try {
			return seasons[yr][szn];
		} catch (IndexOutOfBoundsException e) {
			return UNDEF;
		}
	}


	/* Concrete implementation members */

	private final String szn;
	private final int year;

	private Season(String s, int y) {
		szn = s;
		year = y;
	}

	@SuppressWarnings("preview")
	@Override
	public final boolean equals(Object o) {
		// identity check
		if (this == o)
			return true;

		// null and type check
		if (o instanceof Season other) {
			return year == other.year && szn.equals(other.szn);
		}
		return false;
	}

	@Override
	public final int hashCode() {
		return Objects.hash(szn, year);
	}

	@Override
	public String toString() {
		if (this.equals(UNDEF))
			return "-";

		return szn + " " + year;
	}

	@Override
	public int compareTo(Season other) {
		return (this.year != other.year) ? this.year - other.year : this.getSeasonInt() - other.getSeasonInt();
	}

	private int getSeasonInt() {
		return Season.getSeasonInt(szn);
	}
}
