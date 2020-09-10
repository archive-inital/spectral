package org.spectral.asm.code

import org.spectral.asm.code.Code
import org.spectral.asm.code.Instruction
import org.spectral.util.collection.asReadOnly

class Instructions(val code: Code) : Iterable<Instruction> {

    val instructions = mutableListOf<Instruction>()

    override fun iterator(): Iterator<Instruction> {
        return this.instructions.iterator().asReadOnly()
    }
}