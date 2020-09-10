package org.spectral.asm.code.instruction

import org.objectweb.asm.Opcodes.ACONST_NULL
import org.spectral.asm.annotation.InstructionData
import org.spectral.asm.code.Instruction
import org.spectral.asm.code.InstructionType
import org.spectral.asm.code.Instructions

@InstructionData(ACONST_NULL, "aconst_null")
class AConstNull(insns: Instructions, type: InstructionType) : Instruction(insns, type) {

    constructor(insns: Instructions) : this(insns, InstructionType.ACONST_NULL)

}