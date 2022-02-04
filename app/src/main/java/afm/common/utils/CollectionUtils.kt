package afm.common.utils

import java.util.EnumSet

open class ImmutableEnumSet<E : Enum<E>> protected constructor(val backingSet: EnumSet<E>) : Set<E> by backingSet {

    @Deprecated("Always throws UnsupportedOperationException")
    fun add(v: E): Nothing {
        throw UnsupportedOperationException("Immutable collection")
    }

    override fun equals(other: Any?): Boolean = backingSet == other

    override fun hashCode(): Int = backingSet.hashCode()

    override fun toString(): String = backingSet.toString()
    
    companion object {
        @JvmStatic
        fun <E : Enum<E>> viewOf(values: EnumSet<E>): ImmutableEnumSet<E> = ImmutableEnumSet(values)

        @JvmStatic
        fun <E : Enum<E>> copyOf(values: EnumSet<E>): ImmutableEnumSet<E> = ImmutableEnumSet(EnumSet.copyOf(values))

        inline fun <reified E : Enum<E>> copyOf(values: Iterable<E>): ImmutableEnumSet<E> {
            // use alternative constructor in case provided set is empty
            return viewOf(EnumSet.noneOf(E::class.java).apply { addAll(values) })
        }

        inline fun <reified E: Enum<E>> build(init: EnumSet<E>.() -> Unit): ImmutableEnumSet<E> {
            return viewOf(EnumSet.noneOf(E::class.java).apply(init))
        }
    }
}

fun <E : Enum<E>> EnumSet<E>.immutable() = ImmutableEnumSet.copyOf(this)

inline fun <reified E : Enum<E>> emptyEnumSet(): EnumSet<E> = EnumSet.noneOf(E::class.java)

fun <E> MutableCollection<E>.setAll(elements: Collection<E>): Boolean {
    clear()
    return addAll(elements)
}
