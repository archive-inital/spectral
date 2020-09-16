package org.spectral.matcher.util

import org.objectweb.asm.Opcodes.INVOKESTATIC
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.spectral.asm.ClassPool

object SimulationUtil {

    /**
     * Gets whether a [MethodInsnNode] is of a matchable class.
     *
     * @param pool MutableList<ClassNode>
     * @param insn MethodInsnNode
     * @return Boolean
     */
    fun isMatchable(pool: ClassPool, insn: MethodInsnNode): Boolean {
        val method = pool[insn.owner]?.methods?.firstOrNull { it.name == insn.name && it.desc == insn.desc }
            ?: return false

        val clsName = method.owner.name

        if(clsName.startsWith("java/lang/reflect") || clsName.startsWith("java/io") || clsName.startsWith("java/util/")) {
            return true
        }

        if(clsName.startsWith("java/") || clsName.startsWith("netscape/") || clsName.startsWith("javax/")) {
            return false
        }

        return true
    }

    /**
     * Gets whether a give instruction can be inlined.
     *
     * @param pool MutableList<ClassNode>
     * @param insn AbstractInsnNode
     * @return Boolean
     */
    fun isInlineable(pool: ClassPool, insn: AbstractInsnNode): Boolean {
        if(insn.opcode != INVOKESTATIC) return false
        if(insn !is MethodInsnNode) return false

        val method = pool[insn.owner]?.methods?.firstOrNull { it.name == insn.name && it.desc == insn.desc }
            ?: return false

        return pool[method.owner.name] != null
    }
}