package afm.screens.settings;

import javax.annotation.Nonnull;

public enum Category {

	LOADING,
	SEARCH,
	VISUAL,
	OTHER,
	;

	@Override
	public @Nonnull String toString() {
		String name = this.name();
		return name.charAt(0) + name.substring(1).toLowerCase();
	}
}
