package org.spectral.asm

/**
 * Represents a collection of [Class] objects from a common class path source.
 * @constructor Creates an empty [ClassPool].
 */
class ClassPool {

    /**
     * The [Class] objects stored in this pool.
     */
    val classes = mutableListOf<Class>()

    private val classMap = hashMapOf<String, Class>()

    /**
     * Adds a [Class] to the pool.
     * @param element Class
     */
    fun addClass(element: Class) {
        assert(element.pool == this)

        element.pool = this
        classes.add(element)
        classMap[element.name] = element
    }

    /**
     * Removes a [Class] from the pool.
     * @param element Class
     */
    fun removeClass(element: Class) {
        classes.remove(element)
        classMap.remove(element.name)
    }
}