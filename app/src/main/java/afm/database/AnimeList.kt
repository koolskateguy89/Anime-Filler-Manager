package afm.database

import afm.Main
import afm.anime.Anime
import afm.user.Settings
import javafx.collections.FXCollections
import javafx.collections.ObservableSet
import javafx.collections.SetChangeListener
import java.util.TreeSet

// Delegate actions for MyList & ToWatch to not duplicate code
// https://stackoverflow.com/a/50308477/17381629

sealed interface AnimeList {
    val added: Set<Anime>
    val removedNames: Set<String>
    fun addSilent(anime: Anime)
    fun add(anime: Anime)
    fun addAll(col: Collection<Anime>)
    fun remove(anime: Anime)
    operator fun contains(anime: Anime): Boolean
    val size: Int
    fun clear()
    fun values(): Set<Anime>
}

/*
 * Can't lie my implementation of {name order} is absolutely genius:
 * -The database always stays in insertion order
 * -Enforce name order by using a TreeSet for runTime, database insertion order
 *    is still maintained as added is always a LinkedHS (insertion order)
 * -Enforce insertion order by using a LinkedHS as the backing set
 */
private class AnimeListImpl(private val refreshTable: () -> Unit) : AnimeList {

    private val runTime: ObservableSet<Anime> = FXCollections.observableSet(
        if (Settings.get(Settings.Key.NAME_ORDER))
            TreeSet(Anime.SORT_BY_NAME)
        else
            LinkedHashSet()
    ).apply {
        addListener(SetChangeListener { refreshTable() })
    }

    override val added = LinkedHashSet<Anime>()

    // Only the name of the removed anime is important
    private val removed = mutableSetOf<String>()
    override val removedNames: Set<String> = removed

    /* add anime to runTime without adding to {added}.
	 * used when loading anime from database
	 */
    override fun addSilent(anime: Anime) {
        runTime.add(anime)
    }

    override fun add(anime: Anime) {
        runTime.add(anime)
        removed.remove(anime.name)
        added.add(anime)
    }

    override fun addAll(col: Collection<Anime>) = col.forEach { add(it) }

    override fun remove(anime: Anime) {
        // only add to `removed` if the anime was present in `runTime`
        if (runTime.remove(anime))
            removed.add(anime.name)

        added.remove(anime)
    }

    override operator fun contains(anime: Anime): Boolean = anime in runTime

    override val size: Int
            get() = runTime.size

    override fun clear() = runTime.clear()

    override fun values(): ObservableSet<Anime> = runTime
}


@JvmField
val MyListKt: AnimeList = AnimeListImpl {
    Main.getInstance().myListScreen.refreshTable()
}

@JvmField
val ToWatchKt: AnimeList = AnimeListImpl {
    Main.getInstance().toWatchScreen.refreshTable()
}
