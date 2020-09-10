package org.spectral.asm.code.instruction

import org.objectweb.asm.Opcodes
import org.spectral.asm.annotation.InstructionData
import org.spectral.asm.code.Instruction
import org.spectral.asm.code.InstructionType
import org.spectral.asm.code.Instructions

@InstructionData(Opcodes.NOP, "nop")
open class Nop(insns: Instructions, type: InstructionType) : Instruction(insns, type) {

    constructor(insns: Instructions) : this(insns, InstructionType.NOP)

}