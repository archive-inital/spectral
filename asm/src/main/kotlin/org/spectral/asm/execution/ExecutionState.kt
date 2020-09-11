package org.spectral.asm.execution

import me.coley.analysis.value.AbstractValue
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.analysis.Frame
import org.spectral.asm.Method
import org.spectral.asm.execution.context.StackContext
import org.spectral.asm.execution.context.VariableContext
import java.util.*

/**
 * Represents the JVM stack after [insn] has been executed. This is basically
 * representing a snapshot in time during the method execution after a given instruction has
 * executed.
 *
 * @property insn AbstractInsnNode
 * @constructor
 */
class ExecutionState(val insn: AbstractInsnNode, frame: Frame<AbstractValue>) {

    /**
     * The previous execution state.
     * If this state instance is an entrypoint, this value can be
     * null.
     */
    var prev: ExecutionState? = null

    /**
     * The Next execution state.
     * If this state instance is a terminal state, this value will be null.
     */
    var next: ExecutionState? = null

    /**
     * The values which got pushed to the stack during this execution
     * state.
     */
    val pushes = mutableListOf<StackContext>()

    /**
     * The values which were popped off the stack during this execution state.
     */
    val pops = mutableListOf<StackContext>()

    /**
     * The variables which where read from off the LVT during this execution state.
     */
    val reads = mutableListOf<VariableContext>()

    /**
     * The methods which were invoked during this execution state.
     */
    val invokes = mutableListOf<Method>()

    /**
     * The JVM stack and the values on it at this execution state.
     */
    val stack = Stack<AbstractValue>()

    /**
     * The local variable table values at this execution state.
     */
    val localVariableTable = mutableListOf<AbstractValue>()

    /**
     * Initialize the values from the frame
     */
    init {
        /*
         * Populate the stack
         */
        for(i in 0 until frame.stackSize) {
            stack.push(frame.getStack(i))
        }

        /*
         * Populate the local variable tables
         */
        for(i in 0 until frame.locals) {
            localVariableTable.add(i, frame.getLocal(i))
        }
    }
}