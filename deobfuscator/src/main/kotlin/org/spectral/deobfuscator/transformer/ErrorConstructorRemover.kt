package org.spectral.deobfuscator.transformer

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.spectral.asm.ClassPool
import org.spectral.asm.Method
import org.spectral.deobfuscator.Transformer
import org.spectral.deobfuscator.common.Transform
import org.tinylog.kotlin.Logger

/**
 * Removes contructors which handle the error exception throwing.
 * This is required because when removing all RuntimeExceptions,
 * some of the left over constructors are not handled.
 */
@Transform(priority = 7)
class ErrorConstructorRemover : Transformer {

    override fun transform(pool: ClassPool) {
        var counter = 0

        pool.classes.forEach { c ->
            val methodIterator = c.methods.iterator()
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

    private val Method.hasErrorConstructor: Boolean get() {
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