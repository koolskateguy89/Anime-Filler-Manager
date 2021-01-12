package afm.database;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;

import afm.anime.Anime;
import afm.user.Settings;
import afm.utils.Handler;

public final class ToWatch {

	// don't allow any instantiations of this to be made
	private ToWatch() { }

	private static final ObservableSet<Anime> runTime;

	static {
		Set<Anime> backingSet = Settings.nameOrder() ? new TreeSet<>(Anime.SORT_BY_NAME) : new LinkedHashSet<>();
		runTime = FXCollections.observableSet(backingSet);
	}

	public static void init(Handler handler) {
		SetChangeListener<Anime> changeListener = change -> handler.getMain().toWatchScreen.refreshTable();
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

	public static void update(Anime anime) {
		// For when an anime already exists in runTime, set it to be updated
		// in database
		if (!runTime.add(anime)) {
			added.remove(anime);
		}
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
			anime.close();
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

	// changes made by iterator are be reflected in map
	public static Iterator<Anime> iterator() {
		return values().iterator();
	}

	static Set<Anime> getAdded() {
		return added;
	}

	static String getRemovedSQL() {
		return removed.stream()
					  .map(name -> name.replace("'", "''"))	// escape quotes in SQL
					  .map(name -> new StringBuilder(name).insert(0, '\'').append('\'').toString())
					  .collect(Collectors.joining(","));
	}
}
