package org.spectral.deobfuscator.transformer.opaque

import org.spectral.asm.ClassPool
import org.spectral.deobfuscator.Transformer
import org.spectral.deobfuscator.common.Transform

/**
 * Removes the logic that checks the values passed in the opaque predicates in some
 * methods.
 *
 * E.g.
 *
 * if(var3 == -3409283420) {
 *      throw IllegalStateException()
 * }
 *
 * This transformer will leave the last arguments that are opaque predicates unused in the client
 * ass well as it will also leave dead code / methods which are no longer reachable.
 */
@Transform(priority = 2)
class OpaquePredicateExceptionRemover : Transformer {

    override fun transform(pool: ClassPool) {

    }

    /*
     * =======================================================
     * = UTILITY METHODS
     * =======================================================
     */
}