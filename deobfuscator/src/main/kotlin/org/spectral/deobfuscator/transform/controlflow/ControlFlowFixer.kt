package org.spectral.deobfuscator.transform.controlflow

import me.coley.analysis.SimAnalyzer
import me.coley.analysis.SimInterpreter
import org.spectral.asm.core.ClassPool
import org.spectral.deobfuscator.Transformer

/**
 * Re-orders the control flow blocks in methods.
 */
class ControlFlowFixer : Transformer {

    override fun execute(pool: ClassPool) {
        pool.forEach { c ->
            c.methods.forEach { m ->
            }
        }
    }
}