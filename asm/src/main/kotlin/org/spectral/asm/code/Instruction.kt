package org.spectral.asm.code

import org.objectweb.asm.MethodVisitor

/**
 * Represents an abstract bytecode instruction.
 *
 * @property insns Instructions
 * @property type InstructionType
 * @constructor
 */
abstract class Instruction(var insns: Instructions, var type: InstructionType) : Cloneable {

    /**
     * Whether this instruction terminates a control flow block.
     */
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

    /**
     * Accepts a visitor to visit this instruction.
     *
     * @param visitor MethodVisitor
     */
    open fun accept(visitor: MethodVisitor) {
        visitor.visitInsn(this.type.opcode)
    }

    /**
     * Regenerates the constant pool of this instruction.
     */
    open fun regeneratePool() { }

    /**
     * Renames any class references of values and types in this instruction.
     *
     * @param oldName String
     * @param newName String
     */
    open fun renameClass(oldName: String, newName: String) { }


}

