package org.spectral.asm

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.spectral.asm.code.Code
import org.spectral.asm.signature.Signature

/**
 * Represents a method residing inside of a [Class] object.
 *
 * @property owner Class the method resides in.
 * @property name Name of the method
 * @property signature Signature of the method.
 * @constructor Creates a new [Method] instance with set [owner], [name], and [signature] values.
 */
class Method(val owner: Class, override var name: String, var signature: Signature) : Node {

    /**
     * The bit-packed access and visibility flags.
     */
    override var access: Int = -1

    /**
     * The ASM method [Type] object.
     */
    override lateinit var type: Type

    /**
     * The [Code] block of instructions for this method.
     */
    lateinit var code: Code

    /**
     * A list of exception class names which can be thrown by this method.
     */
    val exceptions = Exceptions()

    /**
     * Gets whether this method is a native access or not.
     */
    var isNative: Boolean get() = (access and ACC_NATIVE) != 0
        /**
         * Sets whether this method is native or not.
         * @param value True or False [Boolean]
         */
        set(value) {
            access = when(value) {
                true -> access or ACC_NATIVE
                false -> access and ACC_NATIVE.inv()
            }
        }

    /**
     * Gets whether this method is static or not.
     */
    var isStatic: Boolean get() = (access and ACC_STATIC) != 0

        /**
         * Sets whether this method is static or not.
         * @param value True or false [Boolean]
         */
        set(value) {
            access = when(value) {
                true -> access or ACC_STATIC
                false -> access and ACC_STATIC.inv()
            }
        }

    /**
     * Gets whether this method is final or not.
     */
    var isFinal: Boolean get() = (access and ACC_FINAL) != 0
        /**
         * Sets whether this method is final or not.
         * @param value True or False [Boolean]
         */
        set(value) {
            access = when(value) {
                true -> access or ACC_FINAL
                false -> access and ACC_FINAL.inv()
            }
        }

    /**
     * Gets whether this method is a private method or not.
     */
    var isPrivate: Boolean get() = (access and ACC_PRIVATE) != 0
        /**
         * Sets whether this method is private or not.
         *
         * If this value is set to false, the method is given the ACC_PUBLIC visibility flag to its
         * access flags.
         *
         * @param value True or False [Boolean]
         */
        set(value) {
            access = if(value) {
                ((access and VISIBILITY_MODIFIERS.inv()) or ACC_PRIVATE)
            } else {
                ((access and VISIBILITY_MODIFIERS.inv()) or ACC_PUBLIC)
            }
        }

    /**
     * Gets a string representation of this method.
     *
     * Format '<class name>.<method name><method descriptor>'
     *
     * @return String
     */
    override fun toString(): String {
        return "$owner.$name$signature"
    }

    companion object {
        /**
         * Visibility access flag modifiers as a single bit-packed integer.
         */
        private const val VISIBILITY_MODIFIERS = ACC_PUBLIC or ACC_PRIVATE or ACC_PROTECTED
    }
}