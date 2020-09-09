package org.spectral.deobfuscator.transform

import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicInterpreter
import org.spectral.asm.core.ClassPool
import org.spectral.deobfuscator.Transformer
import org.tinylog.kotlin.Logger

/**
 * Removes any dead instruction frames which are unreachable in a control flow graph.
 */
class DeadCodeRemover : Transformer {

    private var counter = 0

    override fun execute(pool: ClassPool) {
        pool.values.forEach { c ->
            c.methods.forEach { m ->
                try {
                    val frames = Analyzer(BasicInterpreter()).analyze(c.name, m.node)
                    val insns = m.node.instructions.toArray()

                    for(i in frames.indices) {
                        if(frames[i] == null) {
                            m.node.instructions.remove(insns[i])
                            counter++
                        }
                    }
                } catch(e : Exception) {
                    Logger.error { "Failed to remove instruction frame in method: $m"}
                }
            }
        }

        Logger.info("Removed $counter dead instruction frames.")
    }
}