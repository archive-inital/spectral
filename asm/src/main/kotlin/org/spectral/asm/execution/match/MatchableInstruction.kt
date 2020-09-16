package org.spectral.asm.execution.match

import org.spectral.asm.execution.ExecutionState

interface MatchableInstruction {

    fun map(matchGroup: MatchGroup, state: ExecutionState, other: ExecutionState) { throw UnsupportedOperationException() }

    fun isSame(a: ExecutionState, b: ExecutionState): Boolean { return true }

    fun canMap(state: ExecutionState): Boolean { throw UnsupportedOperationException() }

}