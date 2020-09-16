package org.spectral.asm.execution.match

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.tree.AbstractInsnNode
import org.spectral.asm.execution.match.instruction.ArrayLoad

enum class MatchableInstructionType(val insn: MatchableInstruction, vararg val opcodes: Int) {

    ARRAY_LOAD(ArrayLoad, IALOAD, BALOAD, AALOAD, FALOAD, DALOAD, CALOAD, LALOAD, SALOAD);

    companion object {

        val values = enumValues<MatchableInstructionType>()

        fun forInsn(insn: AbstractInsnNode): MatchableInstructionType? {
            return values.firstOrNull { insn.opcode in it.opcodes }
        }

        fun forOpcode(opcode: Int): MatchableInstructionType? {
            return values.firstOrNull { opcode in it.opcodes }
        }
    }
}