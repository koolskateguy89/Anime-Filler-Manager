package afm.database;

import java.util.Collection;
import java.util.Set;

import afm.Main;
import afm.anime.Anime;
import afm.user.Settings;

final class ToWatch {

	// don't allow any instantiations of this to be made
	private ToWatch() { }

	// delegate all actions to this
	private static final FuncDelegate DELEGATE;

	static {
		boolean nameOrder = Settings.get(Settings.Key.NAME_ORDER);
		Runnable refreshTable = Main.getInstance().toWatchScreen::refreshTable;

		DELEGATE = new FuncDelegate(refreshTable);
	}

	public static void init() {
		DELEGATE.init();
	}

	static void addSilent(Anime anime) {
		DELEGATE.addSilent(anime);
	}

	public static void add(Anime anime) {
		DELEGATE.add(anime);
	}

	public static void addAll(Collection<Anime> col) {
		DELEGATE.addAll(col);
	}

	public static void remove(Anime anime) {
		DELEGATE.remove(anime);
	}

	public static boolean contains(Anime anime) {
		return DELEGATE.contains(anime);
	}

	public static int size() {
		return DELEGATE.size();
	}

	public static void clear() {
		DELEGATE.clear();
	}

	public static Set<Anime> values() {
		return DELEGATE.values();
	}

	static Set<Anime> getAdded() {
		return DELEGATE.getAdded();
	}

	static String getRemovedSQL() {
		return DELEGATE.getRemovedSQL();
	}
}
