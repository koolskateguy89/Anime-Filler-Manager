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

internal open class ImmutableEnumSet<E : Enum<E>>(values: Collection<E>) : Set<E> by EnumSet.copyOf(values) {
    @Deprecated("Always throws UnsupportedOperationException")
    fun add(v: E): Nothing {
        throw UnsupportedOperationException("Immutable collection")
    }
}
