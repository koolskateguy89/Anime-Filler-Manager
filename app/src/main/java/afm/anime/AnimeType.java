package afm.anime;

import javax.annotation.Nonnull;

public enum AnimeType {
	TV,
	OVA,
	MOVIE,
	SPECIAL,
	ONA,
	MUSIC,  // MAL but not the API i got this from
	UNKNOWN,
	;

	public static @Nonnull AnimeType valueOfOrUnknown(@Nonnull String name) {
		for (AnimeType type : values()) {
			if (type.name().equalsIgnoreCase(name))
				return type;
		}
		return UNKNOWN;
	}
}
