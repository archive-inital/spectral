package org.spectral.asm

/**
 * Represents an java class file or object.
 *
 * @property pool The pool this object belongs in.
 * @constructor
 */
class Class(var pool: ClassPool) {

    /**
     * Creates a class object with its own pool membership.
     *
     * @constructor
     */
    constructor() : this(ClassPool()) {
        pool.addClass(this)
    }

    /**
     * The name of this class object.
     */
    lateinit var name: String
        internal set

    /**
     * Gets a string representation of this object.
     *
     * @return String
     */
    override fun toString(): String {
        return name
    }
}