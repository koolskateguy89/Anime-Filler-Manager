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
	
	private Genre() {
		index = findIndex(this.name());
	}
	private Genre(String info) {
		this();
		this.info = info;
	}
	
	@Override public String toString() {
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
		switch (s) {
			case "Action":
				return 1;
			case "Adventure":
				return 2;
			case "Cars":
				return 3;
			case "Comedy":
				return 4;
			case "Dementia":
				return 5;
			case "Demons":
				return 6;
			case "Drama":
				return 8;
			case "Ecchi":
				return 9;
			case "Fantasy":
				return 10;
			case "Game":
				return 11;
			case "Harem":
				return 35;
			case "Hentai":
				return 12;
			case "Historical":
				return 13;
			case "Horror":
				return 14;
			case "Josei":
				return 43;
			case "Kids":
				return 15;
			case "Magic":
				return 16;
			case "MartialArts":
				return 17;
			case "Mecha":
				return 18;
			case "Military":
				return 38;
			case "Music":
				return 19;
			case "Mystery":
				return 7;
			case "Parody":
				return 20;
			case "Police":
				return 39;
			case "Psychological":
				return 40;
			case "Romance":
				return 22;
			case "Samurai":
				return 21;
			case "School":
				return 23;
			case "SciFi":
				return 24;
			case "Sci-Fi":
				return 24;
			case "Seinen":
				return 42;
			case "Shoujo":
				return 25;
			case "ShoujoAi":
				return 26;
			case "Shounen":
				return 27;
			case "ShounenAi":
				return 28;
			case "SliceOfLife":
				return 36;
			case "Space":
				return 29;
			case "Sports":
				return 30;
			case "SuperPower":
				return 31;
			case "Supernatural":
				return 37;
			case "Thriller":
				return 41;
			case "Vampire":
				return 32;
			case "Yaoi":
				return 33;
			case "Yuri":
				return 34;
			default:
				throw new IllegalArgumentException("Not a genre: " + s);
		}
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
