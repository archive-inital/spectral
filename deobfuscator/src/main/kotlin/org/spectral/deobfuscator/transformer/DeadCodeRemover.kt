package org.spectral.deobfuscator.transformer

import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.AnalyzerException
import org.objectweb.asm.tree.analysis.BasicInterpreter
import org.objectweb.asm.util.Printer
import org.spectral.asm.ClassPool
import org.spectral.deobfuscator.Transformer
import org.spectral.deobfuscator.common.Transform
import org.tinylog.kotlin.Logger

/**
 * Removes any code which cannot be reached during a method execution.
 */
@Transform(priority = 3)
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
                } catch(e : AnalyzerException) {
                    Logger.warn { "Failed to remove dead code frame in method: '$m'." }

                    /*
                     * Print 5 instructions before and after the problem.
                     */
                    val errorInsnIndex = m.instructions.indexOf(e.node)
                    for(i in errorInsnIndex - 5 until errorInsnIndex + 5) {
                        val insn = m.instructions[i]

                        val marker = if(i == errorInsnIndex) " <- error" else ""

                        if(insn.opcode == -1) {
                            println("LABEL $marker")
                        } else {
                            println(Printer.OPCODES[insn.opcode] + " " + marker)
                        }
                    }
                }
            }
        }

        Logger.info("Removed $counter dead code instructions.")
    }
}