package afm.database;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

import afm.Main;
import afm.anime.Anime;
import afm.user.Settings;

public final class ToWatch {

	// don't allow any instantiations of this to be made
	private ToWatch() { }

	private static final ObservableSet<Anime> runTime;

	static {
		Set<Anime> backingSet = Settings.get(Settings.Key.NAMEORDER) ? new TreeSet<>(Anime.SORT_BY_NAME)
																	 : new LinkedHashSet<>();
		runTime = FXCollections.observableSet(backingSet);
	}

	public static void init() {
		SetChangeListener<Anime> changeListener = change -> Main.getInstance().toWatchScreen.refreshTable();
		runTime.addListener(changeListener);
	}

	private static final Set<Anime> added = new LinkedHashSet<>();
	// Only the name of the removed anime is important
	private static final Set<String> removed = new HashSet<>();


	static void addSilent(Anime anime) {
		runTime.add(anime);
	}

	public static void add(Anime anime) {
		runTime.add(anime);
		removed.remove(anime.getName());
		added.add(anime);
	}

	public static void addAll(Collection<Anime> col) {
		for (Anime anime : col) {
			add(anime);
		}
	}

	public static void remove(Anime anime) {
		// only add to [removed] if the anime was present in [runTime]
		if (runTime.remove(anime)) {
			removed.add(anime.getName());
		}

		added.remove(anime);
	}

	public static boolean contains(Anime anime) {
		return runTime.contains(anime);
	}

	public static int size() {
		return runTime.size();
	}

	public static void clear() {
		runTime.clear();
	}

	public static Set<Anime> values() {
		return runTime;
	}

	static Set<Anime> getAdded() {
		return added;
	}

	static String getRemovedSQL() {
		return removed.stream()
					  .map(name -> name.replace("'", "''"))	// escape quotes in SQL
					  .map(name -> '\'' + name + '\'')
					  .collect(Collectors.joining(","));
	}
}
