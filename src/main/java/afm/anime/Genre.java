package afm.anime;

import afm.utils.Utils;


public enum Genre {
	// They should all be CONSTANT_CASE but I really cannot be bothered to do that manually sooooo
	// Plus I would then have to reactor the findIndex(String) method sooooo
	Action, Adventure, Cars, Comedy, Dementia, Demons, Drama, Ecchi("(18+)"), Fantasy, Game, Harem, Hentai("(18+)"), Historical,
	Horror, Josei, Kids, Magic, MartialArts, Mecha, Military, Music, Mystery, Parody, Police, Psychological, Romance, Samurai,
	School, SciFi, Seinen, Shoujo, ShoujoAi, Shounen, ShounenAi, SliceOfLife, Space, Sports, SuperPower, Supernatural, Thriller,
	Vampire, Yaoi("(18+)"), Yuri("(18+)");


	String info;
	/* it is not index in the usual sense, it is index as in
	 * the number in the MyAnimeList link
	 * (used when searching for anime)
	 * */
	int index;

	Genre() {
		index = findIndex(this.name());
	}
	
	Genre(String info) {
		this();
		this.info = info;
	}

	@Override
	public String toString() {
		String[] sp = Utils.splitByCapitals(this.name());

		StringBuilder sb = new StringBuilder(sp[0]);
		for (int i = 1; i < sp.length; i++) {
			sb.append('-').append(sp[i]);
		}
		if (info != null)
			sb.append(' ').append(info);

		return sb.toString();
	}

	public int getIndex() {
		return index;
	}

	// Helper for myanimelist.net link (searching)
	private static int findIndex(String s) {
		return switch (s) {
			case "Action" -> 1;
			case "Adventure" -> 2;
			case "Cars" -> 3;
			case "Comedy" -> 4;
			case "Dementia" -> 5;
			case "Demons" -> 6;
			case "Drama" -> 8;
			case "Ecchi" -> 9;
			case "Fantasy" -> 10;
			case "Game" -> 11;
			case "Harem" -> 35;
			case "Hentai" -> 12;
			case "Historical" -> 13;
			case "Horror" -> 14;
			case "Josei" -> 43;
			case "Kids" -> 15;
			case "Magic" -> 16;
			case "MartialArts" -> 17;
			case "Mecha" -> 18;
			case "Military" -> 38;
			case "Music" -> 19;
			case "Mystery" -> 7;
			case "Parody" -> 20;
			case "Police" -> 39;
			case "Psychological" -> 40;
			case "Romance" -> 22;
			case "Samurai" -> 21;
			case "School" -> 23;
			case "SciFi" -> 24;
			case "Sci-Fi" -> 24;
			case "Seinen" -> 42;
			case "Shoujo" -> 25;
			case "ShoujoAi" -> 26;
			case "Shounen" -> 27;
			case "ShounenAi" -> 28;
			case "SliceOfLife" -> 36;
			case "Space" -> 29;
			case "Sports" -> 30;
			case "SuperPower" -> 31;
			case "Supernatural" -> 37;
			case "Thriller" -> 41;
			case "Vampire" -> 32;
			case "Yaoi" -> 33;
			case "Yuri" -> 34;
			default -> throw new IllegalArgumentException("Not a genre: " + s);
		};
	}

	/* special implementation of binary search that ignores case for safety */
	public static Genre parseGenreFromToString(String toString) {
		var values = values();
		int pos = Utils.binarySearch(values, toString);

		if (pos == -1)
			return null;
		else
			return values[pos];
	}

	// ignore case for safety
	public static Genre getGenre(String ignoreCaseName) {
		// values() is final so this should be optimised by JIT compiler (in-lining)
		for (Genre g : values()) {
			if (g.name().equalsIgnoreCase(ignoreCaseName))
				return g;
		}
		return null;
	}
}
