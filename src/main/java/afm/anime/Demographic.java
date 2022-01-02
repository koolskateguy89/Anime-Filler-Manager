package afm.anime;

import lombok.Getter;

// TODO: Demographic
public enum Demographic implements GenreType {

	Kids(15);

	@Getter
	final int id;

	Demographic(int id) {

		this.id = id;
	}

}
