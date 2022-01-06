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

import afm.anime.Anime;
import afm.user.Settings;

/*
 * A helper class for MyList and ToWatch to delegate actions, as they are pretty much
 * the exact same. This is to reduce code duplication.
 */
final class FuncDelegate implements Funcs {

	final ObservableSet<Anime> runTime;
	final Runnable refreshTable;

	FuncDelegate(Runnable refreshTable) {
		/*
		 * Can't lie my implementation of {name order} is absolutely genius:
		 * -The database always stays in insertion order
		 * -Enforce name order by using a TreeSet for runTime, database insertion order
		 *    is still maintained as added is always a LinkedHS (insertion order)
		 * -Enforce insertion order by using a LinkedHS
		 */
		Set<Anime> backingSet = Settings.get(Settings.Key.NAME_ORDER) ? new TreeSet<>(Anime.SORT_BY_NAME)
																	: new LinkedHashSet<>();
		runTime = FXCollections.observableSet(backingSet);

		this.refreshTable = refreshTable;
	}

	public void init() {
		SetChangeListener<Anime> changeListener = change -> refreshTable.run();
		runTime.addListener(changeListener);
	}

	private static final Set<Anime> added = new LinkedHashSet<>();
	// Only the name of the removed anime is important
	private static final Set<String> removed = new HashSet<>();

	/* add anime to runTime without adding to {added}.
	 * used when loading anime from database
	 */
	public void addSilent(Anime anime) {
		runTime.add(anime);
	}

	public void add(Anime anime) {
		runTime.add(anime);
		removed.remove(anime.getName());
		added.add(anime);
	}

	public void addAll(Collection<Anime> col) {
		for (Anime anime : col) {
			add(anime);
		}
	}

	public void remove(Anime anime) {
		// only add to `removed` if the anime was present in `runTime`
		if (runTime.remove(anime)) {
			removed.add(anime.getName());
		}

		added.remove(anime);
	}

	public boolean contains(Anime anime) {
		return runTime.contains(anime);
	}

	public int size() {
		return runTime.size();
	}

	public void clear() {
		runTime.clear();
	}

	public Set<Anime> values() {
		return runTime;
	}

	public Set<Anime> getAdded() {
		return added;
	}

	public String getRemovedSQL() {
		return removed.stream()
				.map(name -> name.replace("'", "''"))	// escape quotes in SQL
				.map(name -> '\'' + name + '\'')
				.collect(Collectors.joining(","));
	}

}
