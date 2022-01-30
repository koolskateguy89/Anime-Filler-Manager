package afm.common.utils

import java.util.EnumSet

open class ImmutableEnumSet<E : Enum<E>> private constructor(
    val backingSet: EnumSet<E>,
) : Set<E> by backingSet {

    @Deprecated("Always throws UnsupportedOperationException")
    fun add(v: E): Nothing {
        throw UnsupportedOperationException("Immutable collection")
    }

    override fun equals(other: Any?): Boolean = backingSet == other

    override fun hashCode(): Int = backingSet.hashCode()

    companion object {
        @JvmStatic
        fun <E : Enum<E>> copyOf(values: Collection<E>): ImmutableEnumSet<E> = ImmutableEnumSet(EnumSet.copyOf(values))

        @JvmStatic
        fun <E : Enum<E>> viewOf(values: EnumSet<E>): ImmutableEnumSet<E> = ImmutableEnumSet(values)
    }
}

fun <E : Enum<E>> EnumSet<E>.immutable(): ImmutableEnumSet<E> = ImmutableEnumSet.copyOf(this)

inline fun <reified E : Enum<E>> emptyEnumSet(): EnumSet<E> =
    EnumSet.noneOf(E::class.java)

fun <E> MutableCollection<E>.setAll(elements: Collection<E>): Boolean {
    clear()
    return addAll(elements)
}
