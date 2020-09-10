package org.spectral.asm.code

import org.spectral.util.collection.asReadOnly

/**
 * Represents a collection of [Exception] objects within a [Code] block of a method.
 *
 * @property code Code
 * @property exceptions MutableList<Exception>
 * @constructor
 */
class Exceptions(val code: Code) {

    private val exceptions = mutableListOf<Exception>()

    /**
     * Adds an [Exception] to the collection.
     *
     * @param exception Exception
     */
    fun add(exception: Exception) {
        exceptions.add(exception)
    }

    /**
     * Removes an exception from the collection.
     *
     * @param exception Exception
     */
    fun remove(exception: Exception) {
        exceptions.remove(exception)
    }

    /**
     * Returns this object as a read-only list of [Exception] objects.
     *
     * @return List<Exception>
     */
    fun asList(): List<Exception> = exceptions.asReadOnly()

    /**
     * Removes all exceptions from this collection.
     */
    fun clear() { exceptions.clear() }
}