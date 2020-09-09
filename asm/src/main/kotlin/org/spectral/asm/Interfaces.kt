package org.spectral.asm

import org.spectral.util.collection.asReadOnly

/**
 * Represents a collection of interface class name that [owner] implements.
 *
 * @property owner The implementer [Class].
 * @constructor Creates a new empty interface class name list for a [Class].
 */
class Interfaces(val owner: Class) : Iterable<String> {

    /**
     * The list of interface class names
     */
    private val interfaceNames = mutableListOf<String>()

    /**
     * Gets a read-only [List] of the interface's class names.
     *
     * @return Read-Only [List] of [Class] names.
     */
    fun asList(): List<String> = interfaceNames.asReadOnly()

    /**
     * Adds an interface class name to the collection.
     *
     * @param name The class name of the interface.
     */
    fun addInterface(name: String) {
        if(!interfaceNames.contains(name)) {
            interfaceNames.add(name)
        }
    }

    /**
     * Removes all entries in this interface collection.
     */
    fun clear() {
        interfaceNames.clear()
    }

    /**
     * Gets whether all the interfaces are an instance of another [Class].
     *
     * @param other The other [Class].
     * @return Whether all interfaces are instances of [other].
     */
    fun instanceOf(other: Class): Boolean {
        interfaceNames.forEach { itf ->
            owner.pool[itf]?.instanceOf(other)?.let {
                return true
            }
        }

        return false
    }

    /**
     * Gets the interfaces collection iterator as a read-only iterator.
     *
     * @return Read-Only [Iterator] of type [String]
     */
    override fun iterator(): Iterator<String> {
        return interfaceNames.iterator().asReadOnly()
    }
}