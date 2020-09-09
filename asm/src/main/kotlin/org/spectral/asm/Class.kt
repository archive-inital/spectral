package org.spectral.asm

import org.objectweb.asm.Type

/**
 * Represents an java class file or object.
 *
 * @property pool The [ClassPool] this object belongs in.
 * @constructor Creates an [Class] with a defined [ClassPool] membership.
 */
class Class(override val pool: ClassPool) : Node {

    /**
     * @constructor Creates a [Class] with a newly created [ClassPool] membership.
     */
    constructor() : this(ClassPool()) {
        pool.addClass(this)
    }

    /**
     * The name of the class.
     */
    override lateinit var name: String

    /**
     * The super-type (superClass) class name.
     */
    lateinit var superName: String

    /**
     * The parent or super class [Class].
     *
     * This property is null if the super class does not exist in this
     * object's member [ClassPool].
     */
    var parent: Class? = null

    /**
     * The [Class]s which inherit from this object.
     *
     * This includes both super-classes as well as classes which might implement this class
     * as an interface.
     */
    val children = mutableListOf<Class>()

    /**
     * The source file name this class came from.
     */
    lateinit var source: String

    /**
     * The bit-packed access flags of this class.
     */
    override var access: Int = -1

    /**
     * The ASM [Type] of this object.
     */
    override lateinit var type: Type

    /**
     * The class file version if one is provided.
     */
    var version: Int = -1

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