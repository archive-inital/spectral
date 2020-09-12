package org.spectral.asm.execution.context

import me.coley.analysis.value.AbstractValue
import org.spectral.asm.execution.ExecutionState

/**
 * Represents a resulting value context of a method instruction execution.
 * This context holds information about the value pushed / popped from the stack
 *
 * @property state The [ExecutionState] which pushed this to the stack or LVT
 * @property value The actual stack or LV value object.
 * @constructor
 */
class StackContext(val state: ExecutionState, val value: AbstractValue) {

    /**
     * The [ExecutionState]s which pop this value context off the stack either
     * in the past or in the future.
     */
    val pops = mutableListOf<ExecutionState>()

    /**
     * The execution state that pops this value from the stack as a
     * return value to another execution state reference.
     */
    var returnSource: ExecutionState? = null
}