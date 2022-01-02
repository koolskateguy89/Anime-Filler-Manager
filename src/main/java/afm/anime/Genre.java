package afm.anime;

import lombok.Getter;

import afm.utils.Utils;

// TODO: Demographic & Theme
/*
Might just keep demo & theme in here. Not sure whether to split them up like when showing search results show
them separately. Actually it's probably better to keep them here because some don't have a theme/demo.
 */
public enum Genre implements GenreType {
	
	Action(1),
	Adventure(2),
	AvantGarde(5),
	AwardWinning(46),
	BoysLove(28),
	Cars(3),
	Comedy(4),
	Demons(6),
	Drama(8),
	Ecchi(9, "(18+)"),
	Erotica(49, "(18+)"),
	Fantasy(10),
	Game(11),
	GirlsLove(26),
	Gourmet(47),
	Harem(35),
	Hentai(12, "(18+)"),
	Historical(13),
	Horror(14),
	Josei(43),  // Demographics
	Kids(15),  // Demographics
	Magic(16),
	MartialArts(17),
	Mecha(18),
	Military(38),
	Music(19),
	Mystery(7),
	Parody(20),
	Police(39),
	Psychological(40),
	Romance(22),
	Samurai(21),
	School(23),
	SciFi(24),
	Seinen(42),  // Demographics
	Shoujo(25),  // Demographics
	Shounen(27),  // Demographics
	SliceOfLife(36),
	Space(29),
	Sports(30),
	SuperPower(31),
	Supernatural(37),
	Suspense(41),
	Vampire(32),
	WorkLife(48);


	final String info;
	// the number in the MyAnimeList link of a genre (used when searching for anime)
	@Getter
	final int id;

	Genre(int id) {
		this(id, null);
	}

	Genre(int id, String info) {
		this.id = id;
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

	/* special implementation of binary search that ignores case for safety */
	public static Genre parseGenreFromToString(String toString) {
		Genre[] values = values();
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
