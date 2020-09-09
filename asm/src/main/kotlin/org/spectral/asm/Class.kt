package org.spectral.asm

/**
 * Represents an java class file or object.
 *
 * @property pool The pool this object belongs in.
 * @constructor Creates an [Class] with a defined [ClassPool] membership.
 */
class Class(var pool: ClassPool) {

    /**
     * @constructor Creates a [Class] with a newly created [ClassPool] membership.
     */
    constructor() : this(ClassPool()) {
        pool.addClass(this)
    }

    /**
     * The name of the class.
     */
    lateinit var name: String
        internal set

    /**
     * Gets a string representation of the class.
     *
     * The format is '[name]'
     *
     * @return String
     */
    override fun toString(): String {
        return name
    }
}