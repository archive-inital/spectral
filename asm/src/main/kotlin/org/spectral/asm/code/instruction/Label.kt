package org.spectral.asm.code.instruction

import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.spectral.asm.code.Instruction
import org.spectral.asm.code.Instructions

/**
 * Represents the LABEL bytecode instruction.
 */
class Label(insns: Instructions) : Nop(insns) {

    /**
     * The ASM [Label] instance
     */
    lateinit var label: Label

    /**
     * The line number this label is at in the bytecode.
     */
    var lineNumber: Int = -1

    /**
     * Creates a [Label] instance with associated ASM backing label.
     *
     * @param insns Instructions
     * @param label Label
     * @constructor
     */
    constructor(insns: Instructions, label: Label) : this(insns) {
        this.label = label
    }

    override fun clone(): Instruction {
        val l = super.clone() as org.spectral.asm.code.instruction.Label
        l.label = Label()
        l.lineNumber = lineNumber
        return l
    }

    override fun accept(visitor: MethodVisitor) {
        visitor.visitLabel(label)

        if(lineNumber != -1) {
            visitor.visitLineNumber(lineNumber, label)
        }
    }

    /**
     * Gets the next instruction of the labeled frame.
     *
     * @return Instruction
     */
    fun next(): Instruction {
        val insns = insns
        var i = insns.asList().indexOf(this)

        var next: Instruction
        do {
            next = insns.asList()[i + 1]
            ++i
        } while (next is org.spectral.asm.code.instruction.Label)

        return next
    }
}