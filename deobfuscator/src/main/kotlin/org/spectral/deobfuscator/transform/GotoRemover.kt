package org.spectral.deobfuscator.transform

import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LabelNode
import org.spectral.asm.core.ClassPool
import org.spectral.deobfuscator.Transformer
import org.tinylog.kotlin.Logger

class GotoRemover : Transformer {

    private var counter = 0

    override fun execute(pool: ClassPool) {
        pool.values.forEach { c ->
            c.methods.forEach { m ->
                val instructions = m.node.instructions.iterator()
                while(instructions.hasNext()) {
                    val insn0 = instructions.next()

                    if(insn0.opcode != Opcodes.GOTO) continue
                    insn0 as JumpInsnNode

                    val insn1 = insn0.next
                    if(insn1 == null || insn1 !is LabelNode) continue

                    if(insn0.label == insn1) {
                        instructions.remove()
                        counter++
                    }
                }
            }
        }

        Logger.info("Removed $counter redundant GOTO instructions.")
    }
}