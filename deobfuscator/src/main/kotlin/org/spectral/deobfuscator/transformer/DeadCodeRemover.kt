package org.spectral.deobfuscator.transformer

import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicInterpreter
import org.spectral.asm.ClassPool
import org.spectral.deobfuscator.Transformer
import org.spectral.deobfuscator.common.Transform
import org.tinylog.kotlin.Logger

/**
 * Removes any code which cannot be reached during a method execution.
 */
@Transform(priority = 2)
class DeadCodeRemover : Transformer {

    private var counter = 0

    /**
     * Runs the transformer logic.
     * @param pool ClassPool
     */
    override fun transform(pool: ClassPool) {
        pool.classes.forEach { c ->
            c.methods.forEach { m ->
                try {
                    val frames = Analyzer(BasicInterpreter()).analyze(c.name, m.node)
                    val insns = m.instructions.toArray()

                    for(i in frames.indices) {
                        if(frames[i] == null) {
                            m.instructions.remove(insns[i])
                            counter++
                        }
                    }
                } catch(e : Exception) {
                    Logger.warn(e) { "Failed to remove dead code frame in method: '$m'." }
                }
            }
        }

        Logger.info("Removed $counter dead code instructions.")
    }
}