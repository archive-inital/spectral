package org.spectral.deobfuscator.transform

import org.objectweb.asm.Opcodes.GOTO
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicInterpreter
import org.spectral.asm.core.ClassPool
import org.spectral.deobfuscator.Transformer
import org.tinylog.kotlin.Logger

/**
 * Removes GOTO instructions which point to the immediate succession instructions.
 * Example:
 *
 * GOTO L1
 * L1:
 *  FRAME SAME
 *  ...
 */
class GotoRemover : Transformer {

    private var counter = 0

    override fun execute(pool: ClassPool) {
        pool.forEach { c ->
            c.methods.forEach { m ->
                val it = m.node.instructions.iterator()
                while(it.hasNext()) {
                    val insn = it.next()

                    if(insn.opcode == GOTO) {
                        insn as JumpInsnNode

                        val jumpInsn = insn.next
                        if(jumpInsn == null || jumpInsn !is LabelNode) continue

                        if(insn.label == jumpInsn) {
                            it.remove()
                            counter++
                        }
                    }
                }
            }
        }

        Logger.info("Removed $counter GOTO instructions.")
    }
}