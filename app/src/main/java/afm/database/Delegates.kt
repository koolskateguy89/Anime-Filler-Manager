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

// TODO: rename file to something more descriptive

// TODO: rename this? but what to? "MyListLike", idk?
interface Funcs {
    fun init()
    fun addSilent(anime: Anime)
    fun add(anime: Anime)
    fun addAll(col: Collection<Anime>)
    fun remove(anime: Anime)
    operator fun contains(anime: Anime): Boolean
    fun size(): Int
    fun clear()
    fun values(): Set<Anime>
    fun getAdded(): Set<Anime>
    fun getRemovedSQL(): String
}

/*
 * Can't lie my implementation of {name order} is absolutely genius:
 * -The database always stays in insertion order
 * -Enforce name order by using a TreeSet for runTime, database insertion order
 *    is still maintained as added is always a LinkedHS (insertion order)
 * -Enforce insertion order by using a LinkedHS as the backing set
 */
private class Delegate(private val refreshTable: Runnable) : Funcs {

    private val runTime: ObservableSet<Anime> = FXCollections.observableSet(
        if (Settings.get(Settings.Key.NAME_ORDER))
            TreeSet(Anime.SORT_BY_NAME)
        else
            LinkedHashSet()
    )

    private val added: MutableSet<Anime> = LinkedHashSet()

    // Only the name of the removed anime is important
    private val removed: MutableSet<String> = HashSet()

    override fun init() {
        runTime.addListener(SetChangeListener { refreshTable.run() })
    }

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

    override fun addAll(col: Collection<Anime>) = col.forEach(::add)

    override fun remove(anime: Anime) {
        // only add to `removed` if the anime was present in `runTime`
        if (runTime.remove(anime))
            removed.add(anime.name)

        added.remove(anime)
    }

    override operator fun contains(anime: Anime): Boolean = anime in runTime

    override fun size(): Int = runTime.size

    override fun clear() = runTime.clear()

    override fun values(): Set<Anime> = runTime

    override fun getAdded(): Set<Anime> = added

    override fun getRemovedSQL(): String {
        return removed
            .map { "'${it.replace("'", "''")}'" } // escape quotes in SQL
            .joinToString(",")
    }
}


@JvmField
val MyListKt: Funcs = Delegate(
    Main.getInstance().myListScreen::refreshTable
)

@JvmField
val ToWatchKt: Funcs = Delegate(
    Main.getInstance().toWatchScreen::refreshTable
)
