package org.spectral.asm.signature

import org.objectweb.asm.Type

/**
 * Represents a method argument types and return type as a identifiable signature.
 *
 * @property args A list of argument [Type]
 * @property ret Return [Type]
 * @constructor Creates a signature with defined [args] and [ret] [Type]'s
 */
class Signature(val args: List<Type>, var ret: Type) {

    /**
     * Creates a signature instance from an [other] [Signature] object.
     *
     * @param other The other [Signature] object.
     * @constructor Create a new [Signature] instance from a different [Signature] object.
     */
    constructor(other: Signature) : this(other.args, other.ret)

    /**
     * Creates a signature instance from a method [Type] object.
     *
     * @param type The method [Type] object
     * @constructor Create a new [Signature] from an ASM method [Type] object.
     */
    constructor(type: Type) : this(type.argumentTypes.toMutableList(), type.returnType)

    companion object {
        /**
         * A Builder for creating a [Signature] instance.
         */
        class Builder {
            private val args = mutableListOf<Type>()
            private var ret: Type? = null

            /**
             * Sets the return type for the builder.
             *
             * @param type [Type]
             * @return [Builder]
             */
            fun setReturnType(type: Type) = this.apply { ret = type }

            /**
             * Adds an argument type for the builder.
             *
             * @param type [Type]
             * @return [Builder]
             */
            fun addArgument(type: Type) = this.apply { args.add(type) }

            /**
             * Builds the [Signature] instance.
             *
             * @return Signature
             */
            fun build(): Signature {
                if(ret == null) {
                    throw IllegalStateException("Return type must be set in signature builder.")
                }

                return Signature(args, ret!!)
            }
        }
    }
}