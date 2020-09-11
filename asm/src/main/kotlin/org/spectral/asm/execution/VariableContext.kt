package org.spectral.asm.execution

import me.coley.analysis.value.AbstractValue

/**
 * Represents a variable that has been accessed or modified from the
 * local variable table.
 *
 * @property state ExecutionState?
 * @property value AbstractValue
 * @constructor
 */
class VariableContext(val state: ExecutionState?, val value: AbstractValue) {

    /**
     * The associated stack context with this object.
     */
    lateinit var stackContext: StackContext

    /**
     * Whether the variable accessed is a parameter of the parent method.
     */
    var isParameter: Boolean = false

    /**
     * The execution states which read the value from this context at some
     * point in the method execution.
     */
    val reads = mutableListOf<ExecutionState>()

    /**
     * Used for entry points. All other times, the method already
     * will have a previous execution state.
     *
     * @param value AbstractValue
     * @constructor
     */
    constructor(value: AbstractValue) : this(null, value)

    /**
     * @param state ExecutionState
     * @param stackContext StackContext
     * @constructor
     */
    constructor(state: ExecutionState, stackContext: StackContext) : this(state, stackContext.value) {
        this.stackContext = stackContext
    }

    constructor(state: ExecutionState, other: VariableContext) : this(state, other.value) {
        this.stackContext = other.stackContext
    }


}