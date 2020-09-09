package org.spectral.asm

import org.objectweb.asm.Type
import kotlin.properties.Delegates

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
    override var name: String by Delegates.observable("") { _, _, value ->
        name = value.replace(".", "/")
    }

    /**
     * The super-type (superClass) class name.
     */
    var superName: String by Delegates.observable("") { _, _, value ->
        superName = value.replace(".", "/")
    }

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
     * The interfaces this class implements.
     */
    val interfaces = Interfaces(this)

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
     * Gets whether this class is an instance of [other]'s [Class].
     *
     * @param other The [Class] to compare.
     * @return The result as a [Boolean].
     */
    fun instanceOf(other: Class): Boolean {
        return this == other ||
                interfaces.instanceOf(other) ||
                (parent != null && parent?.instanceOf(other) ?: false)
    }

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