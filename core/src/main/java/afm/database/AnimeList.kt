package afm.database

import afm.anime.Anime
import java.util.TreeSet

// Delegate actions for MyList & ToWatch to not duplicate code
// https://stackoverflow.com/a/50308477/17381629

sealed interface AnimeList {
    var onChange: Runnable
    var nameOrder: Boolean
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
private class ObservableSet<T>(
    val backingSet: MutableSet<T>,
    var onAdd: Consumer<T> = Consumer {},
    var onRemove: Consumer<T> = Consumer {},
) : MutableSet<T> by backingSet {

    constructor(backingSet: MutableSet<T>, onChange: Runnable)
            : this(backingSet, { onChange.run() }, { onChange.run() })

    var onChange: Runnable
        get() { error("don't call this") }
        set(value) {
            onAdd = Consumer { value.run() }
            onRemove = onAdd
        }

    override fun add(element: T): Boolean = backingSet.add(element).also { onAdd.accept(element) }

    override fun remove(element: T): Boolean = backingSet.remove(element).also { onRemove.accept(element) }

}
 */

// using Runnable instead of () -> Unit because it's going to be set by Java code not Kotlin
private class AnimeListImpl(refreshTable: Runnable) : AnimeList {

    override var onChange: Runnable = refreshTable

    // default insertion order
    override var nameOrder: Boolean = false

    /*
     * Can't lie my implementation of {name order} is absolutely genius:
     * -The database always stays in insertion order
     * -Enforce name order by using a TreeSet for runTime, database insertion order
     *    is still maintained as added is always a LinkedHS (insertion order)
     * -Enforce insertion order by using a LinkedHS as the backing set
     */
    private val nameOrdered = TreeSet(Anime.SORT_BY_NAME)
    private val insertionOrder = LinkedHashSet<Anime>()

    override val added = LinkedHashSet<Anime>()

    // Only the name of the removed anime is important
    private val removed = mutableSetOf<String>()
    override val removedNames: Set<String> = removed

    /* add anime to runTime without adding to {added}.
	 * used when loading anime from database
	 */
    override fun addSilent(anime: Anime) {
        nameOrdered.add(anime)
        insertionOrder.add(anime)
    }

    override fun add(anime: Anime) {
        addSilent(anime)

        removed.remove(anime.name)
        added.add(anime)
        onChange.run()
    }

    override fun addAll(col: Collection<Anime>) = col.forEach { add(it) }

    override fun remove(anime: Anime) {
        // only add to `removed` if the anime was present in `nameOrdered`
        if (nameOrdered.remove(anime)) {
            insertionOrder.remove(anime)
            removed.add(anime.name)
        }

        added.remove(anime)
        onChange.run()
    }

    override operator fun contains(anime: Anime): Boolean = anime in nameOrdered

    override val size: Int
        get() = nameOrdered.size

    override fun clear() = nameOrdered.clear()

    override fun values(): Set<Anime> = if (nameOrder) nameOrdered else insertionOrder
}

@JvmField
val MyListKt: AnimeList = AnimeListImpl {}

@JvmField
val ToWatchKt: AnimeList = AnimeListImpl {}
