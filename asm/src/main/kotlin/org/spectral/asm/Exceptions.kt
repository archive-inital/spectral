package org.spectral.asm

import org.spectral.util.collection.asReadOnly

/**
 * Represents a collections of exceptions which can be
 * thrown.
 *
 * @constructor Creates an empty exceptions class name collection.
 */
class Exceptions {

    /**
     * A list of class names of exceptions
     */
    private val classNames = mutableListOf<String>()

    /**
     * Gets the class names as a read-only list of strings.
     *
     * @return Read-Only [List] of [String] containing a class name.
     */
    fun asList(): List<String> = classNames.asReadOnly()

    /**
     * Adds an exception class name to this collection.
     *
     * @param className Name of the exception class.
     */
    fun addException(className: String) {
        classNames.add(className)
    }
}