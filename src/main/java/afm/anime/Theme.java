package afm.anime;

import lombok.Getter;

// TODO: Theme
public enum Theme implements GenreType {

	Game(11);

	@Getter
	final int id;

	Theme(int id) {
		this.id = id;
	}
}
