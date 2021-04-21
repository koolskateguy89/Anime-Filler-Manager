package afm.screens.settings;

public enum Category {

	LOADING,
	SEARCH,
	VISUAL,
	OTHER,
	;

	@Override
	public String toString() {
		String name = this.name();
		return name.charAt(0) + name.substring(1).toLowerCase();
	}
}
