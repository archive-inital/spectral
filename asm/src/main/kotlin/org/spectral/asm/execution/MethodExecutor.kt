package org.spectral.asm.execution

import me.coley.analysis.SimAnalyzer
import me.coley.analysis.SimInterpreter
import me.coley.analysis.value.AbstractValue
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.analysis.Frame
import org.spectral.asm.Method
import java.util.*

/**
 * Executes a method by simulating how it's instruction interact with the JVM stack and local variable table.
 *
 * At each instruction of a given method, an [ExecutionState] object is created which is a sort
 * of snapshot in time of both the JVM stack values, and the local variable table values.
 *
 * In addition, the values which are pushed and popped from the stack hold references to the [ExecutionState]
 * instances which pushed or popped them.
 *
 * @constructor
 */
class MethodExecutor(method: Method) {

    /**
     * The simulator analyzer
     */
    private val analyzer = SimAnalyzer(SimInterpreter())

    /**
     * An array of ASM frames at each instruction in the method's
     * control flow graph which hold all the values in the local variable table
     * as well as the stack.
     */
    private val instructionFrames: Array<Frame<AbstractValue>>

    /**
     * The current instruction index of the execution.
     */
    var currentIndex = 0

    /**
     * The current instruction being executed.
     */
    lateinit var currentInsn: AbstractInsnNode

    /**
     * An internal model of the JVM stack which changes during
     * the execution.
     */
    private val stack = Stack<ExecutionState>()

    /**
     * Whether the execution is paused or not.
     */
    private var paused = true

    /**
     * Initialize and run the simulation analyzer on the method.
     */
    init {
        analyzer.setSkipDeadCodeBlocks(true)
        analyzer.setThrowUnresolvedAnalyzerErrors(false)
        instructionFrames = analyzer.analyze(method.owner.name, method.node)
    }


}