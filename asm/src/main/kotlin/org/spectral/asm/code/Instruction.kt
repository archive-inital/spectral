package org.spectral.asm.code

import org.objectweb.asm.MethodVisitor

abstract class Instruction(var insns: Instructions, var type: InstructionType) : Cloneable {

    open val terminal: Boolean = false

    override fun clone(): Instruction {
        val i: Instruction
        try {
            i = super.clone() as Instruction
        } catch(e : CloneNotSupportedException) {
            throw RuntimeException(e)
        }

        return i
    }

    open fun accept(visitor: MethodVisitor) {
        visitor.visitInsn(this.type.opcode)
    }

    open fun regeneratePool() { }

    open fun renameClass(oldName: String, newName: String) { }


}

