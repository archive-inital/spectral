package org.spectral.asm.code.instruction

import org.spectral.asm.code.Code
import org.spectral.util.collection.asReadOnly

class Instructions(val code: Code) : Iterable<Instruction> {

    private val instructions = mutableListOf<Instruction>()

    override fun iterator(): Iterator<Instruction> {
        return this.instructions.iterator().asReadOnly()
    }
}