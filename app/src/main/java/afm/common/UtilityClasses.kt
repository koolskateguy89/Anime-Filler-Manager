@file:JvmSynthetic

package afm.common


/*
 * map[key] will return V not V?, so you don't need to use
 *  .getValue nor !!
 *
 * https://stackoverflow.com/a/42709643/17381629
 */
open class NonNullMap<K, V>(private val map: Map<K, V>) : Map<K, V> by map {
    override operator fun get(key: K): V {
        return map[key]!! // Force an NPE if the key doesn't exist
    }
}
