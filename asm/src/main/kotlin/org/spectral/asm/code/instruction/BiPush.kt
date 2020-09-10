package org.spectral.asm.code.instruction

import org.objectweb.asm.Opcodes
import org.spectral.asm.annotation.InstructionData
import org.spectral.asm.code.Instruction
import org.spectral.asm.code.InstructionType
import org.spectral.asm.code.Instructions
import org.spectral.asm.code.instruction.type.IntInstruction

@InstructionData(Opcodes.BIPUSH, "bipush")
class BiPush(insns: Instructions, type: InstructionType) : Instruction(insns, type), IntInstruction {

    override var operand: Int = -1

    constructor(insns: Instructions, operand: Byte) : this(insns, InstructionType.BIPUSH) {
        this.operand = operand.toInt()
    }
}