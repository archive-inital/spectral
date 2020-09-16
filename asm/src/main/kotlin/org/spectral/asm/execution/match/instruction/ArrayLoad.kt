package org.spectral.asm.execution.match.instruction

import org.spectral.asm.execution.ExecutionState
import org.spectral.asm.execution.match.MatchableInstruction

object ArrayLoad : MatchableInstruction {

    override fun isSame(a: ExecutionState, b: ExecutionState): Boolean {
        if(a.insn.type != b.insn.type) {
            return false
        }

        val indexA = a.pops[0]
        val indexB = b.pops[0]

        return false
    }
}