package org.spectral.deobfuscator

import org.spectral.asm.ClassPool

/**
 * Represents a some logic that modifies and simplifies the bytecode of
 * a given [ClassPool] object.
 */
interface Transformer {

    /**
     * Runs the bytecode transformation.
     *
     * @param pool ClassPool
     */
    fun transform(pool: ClassPool)

}