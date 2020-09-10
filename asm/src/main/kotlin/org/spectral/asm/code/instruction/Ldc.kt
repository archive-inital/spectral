package org.spectral.asm.code.instruction

import org.objectweb.asm.Opcodes
import org.spectral.asm.annotation.InstructionData
import org.spectral.asm.code.Instruction
import org.spectral.asm.code.InstructionType
import org.spectral.asm.code.Instructions
import org.spectral.asm.code.instruction.type.PushConstantInstruction

@InstructionData(Opcodes.LDC, "ldc")
class Ldc(insns: Instructions, type: InstructionType) : Instruction(insns, type), PushConstantInstruction {

    override lateinit var constant: Any

    constructor(insns: Instructions, value: Any) : this(insns, InstructionType.LDC) {
        this.constant = value
    }

    fun asNumber(): Number = constant as Number
}