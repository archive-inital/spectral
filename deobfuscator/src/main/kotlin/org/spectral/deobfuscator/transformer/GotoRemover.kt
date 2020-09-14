package org.spectral.deobfuscator.transformer

import org.objectweb.asm.Opcodes.GOTO
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.spectral.asm.ClassPool
import org.spectral.deobfuscator.Transformer
import org.spectral.deobfuscator.common.Transform
import org.tinylog.kotlin.Logger

/**
 * Removes invalid or redundant GOTO jump instructions.
 */
@Transform(priority = 3)
class GotoRemover : Transformer {

    private var counter = 0

    override fun transform(pool: ClassPool) {
        pool.classes.forEach { c ->
            c.methods.forEach { m ->
                val insns = m.instructions.iterator()

                while(insns.hasNext()) {
                    val insn0 = insns.next()

                    if(insn0.opcode != GOTO) continue
                    insn0 as JumpInsnNode

                    val insn1 = insn0.next
                    if(insn1 == null || insn1 !is LabelNode) continue

                    if(insn0.label == insn1) {
                        insns.remove()
                        counter++
                    }
                }
            }
        }

        Logger.info("Removed $counter redundant GOTO instructions.")
    }
}