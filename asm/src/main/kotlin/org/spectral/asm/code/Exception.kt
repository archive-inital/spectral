package org.spectral.asm.code

import org.spectral.asm.code.instruction.Label

/**
 * Represents an exception which can be thrown within a method code block.
 *
 * @property exceptions Exceptions
 * @constructor Creates a new exception with associated [Exceptions] collection it blongs in.
 */
class Exception(private val exceptions: Exceptions) : Cloneable {

    /**
     * The start label of the exception frame
     */
    lateinit var start: Label

    /**
     * The ending label of the exception frame
     */
    lateinit var end: Label

    /**
     * The label where the exception is handled.
     */
    lateinit var handler: Label

    /**
     * The catch exception type class name.
     */
    lateinit var catchType: String


}