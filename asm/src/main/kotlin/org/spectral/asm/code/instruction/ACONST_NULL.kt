package org.spectral.asm.code.instruction

import org.objectweb.asm.Opcodes
import org.spectral.asm.annotation.InstructionData
import org.spectral.asm.code.Instruction
import org.spectral.asm.code.Instructions

@InstructionData(Opcodes.ACONST_NULL, "ACONST_NULL")
class ACONST_NULL(insns: Instructions) : Instruction(insns) {
}