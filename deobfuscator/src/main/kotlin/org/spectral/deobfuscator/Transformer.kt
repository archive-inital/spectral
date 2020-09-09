package org.spectral.deobfuscator

import org.spectral.asm.core.ClassPool

/**
 * Represents a type which transforms the bytecode of each
 * class inside of a class pool.
 */
interface Transformer {

    /**
     * Executes the bytecode transformer logic.
     *
     * @param pool ClassPool
     */
    fun execute(pool: ClassPool)
}