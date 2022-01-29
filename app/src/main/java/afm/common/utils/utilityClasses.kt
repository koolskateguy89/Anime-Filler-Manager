package afm.common.utils

import java.util.EnumSet

/*
 * map[key] will return V not V?, so you don't need to use
 *  .getValue nor !!
 *
 * https://stackoverflow.com/a/42709643/17381629
 */
internal open class NonNullMap<K, V>(private val map: Map<K, V>) : Map<K, V> by map {
    override operator fun get(key: K): V {
        return map[key]!! // Force an NPE if the key doesn't exist
    }
}

open class ImmutableEnumSet<E : Enum<E>> private constructor(
    val backingSet: EnumSet<E>,
) : Set<E> by backingSet {

    constructor(values: Collection<E>) : this(EnumSet.copyOf(values))

    @Deprecated("Always throws UnsupportedOperationException")
    fun add(v: E): Nothing {
        throw UnsupportedOperationException("Immutable collection")
    }

    override fun equals(other: Any?): Boolean = backingSet == other

    override fun hashCode(): Int = backingSet.hashCode()
}

fun <E: Enum<E>> EnumSet<E>.immutable(): ImmutableEnumSet<E> = ImmutableEnumSet(this)
