package org.spectral.deobfuscator.transform

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.MethodNode
import org.spectral.asm.core.ClassPool
import org.spectral.deobfuscator.Transformer
import org.tinylog.kotlin.Logger

/**
 * Removes contructors which handle the error exception throwing.
 * This is required because when removing all RuntimeExceptions,
 * some of the left over constructors are not handled.
 */
class ErrorConstructorRemover : Transformer {

    override fun execute(pool: ClassPool) {
        var counter = 0

        pool.values.forEach { c ->
            val methodIterator = c.node.methods.iterator()
            while(methodIterator.hasNext()) {
                val m = methodIterator.next()
                if(m.hasErrorConstructor) {
                    methodIterator.remove()
                    counter++
                }
            }
        }

        Logger.info("Removed $counter method error constructors.")
    }

    private val MethodNode.hasErrorConstructor: Boolean get() {
        if (this.name != "<init>") return false
        if (Type.getArgumentTypes(this.desc).isNotEmpty()) return false
        if (this.exceptions != listOf(Type.getType(Throwable::class.java).internalName)) return false
        val insns = this.instructions.toArray().filter { it.opcode > 0 }.iterator()
        if (!insns.hasNext() || insns.next().opcode != Opcodes.ALOAD) return false
        if (!insns.hasNext() || insns.next().opcode != Opcodes.INVOKESPECIAL) return false
        if (!insns.hasNext() || insns.next().opcode != Opcodes.NEW) return false
        if (!insns.hasNext() || insns.next().opcode != Opcodes.DUP) return false
        if (!insns.hasNext() || insns.next().opcode != Opcodes.INVOKESPECIAL) return false
        if (!insns.hasNext() || insns.next().opcode != Opcodes.ATHROW) return false
        return !insns.hasNext()
    }
}