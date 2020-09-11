package org.spectral.asm.execution

import me.coley.analysis.SimAnalyzer
import me.coley.analysis.SimInterpreter
import me.coley.analysis.value.AbstractValue
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.analysis.Frame
import org.spectral.asm.Method
import org.spectral.asm.execution.context.StackContext
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
class MethodExecutor(val method: Method) {

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
    var currentIndex = -1

    /**
     * The current instruction being executed.
     */
    lateinit var currentInsn: AbstractInsnNode

    /**
     * The instruction frame that is being processed currently.
     */
    lateinit var currentFrame: Frame<AbstractValue>

    /**
     * The current execution state instance.
     */
    lateinit var currentExecutionState: ExecutionState

    /**
     * A list containing the history of execution states.
     */
    val states = mutableListOf<ExecutionState>()

    /**
     * An internal model of the JVM stack which changes during
     * the execution.
     */
    val stack = Stack<StackContext>()

    /**
     * Whether the execution is paused or not.
     */
    private var paused = true

    /**
     * Whether the execution has terminated or reached the end of the
     * method execution.
     */
    private var terminated = false

    /**
     * A list of [Boolean] predicates which if met will pause the execution.
     */
    private val pausePredicates = mutableListOf<(MethodExecutor) -> Boolean>()

    /**
     * A list of [Boolean] predicates which if met will terminate the current execution.
     */
    private val terminatePredicates = mutableListOf<(MethodExecutor) -> Boolean>()

    /**
     * Initialize and run the simulation analyzer on the method.
     */
    init {
        analyzer.setSkipDeadCodeBlocks(true)
        analyzer.setThrowUnresolvedAnalyzerErrors(false)
        instructionFrames = analyzer.analyze(method.owner.name, method.node)
    }

    /**
     * Gets whether the execution is paused or not.
     *
     * @return Boolean
     */
    fun isPaused(): Boolean = paused

    /**
     * Unpauses the execution.
     */
    fun unpause() {
        paused = false
    }

    /**
     * Pauses the execution.
     */
    fun pause() {
        paused = true
    }

    /**
     * Whether the current execution has been terminated or not.
     *
     * @return Boolean
     */
    fun isTerminated(): Boolean = terminated

    /**
     * Adds a pause predicate to the execution.
     *
     * @param predicate Function1<MethodExecutor, Boolean>
     * @return MethodExecutor
     */
    fun pauseWhen(predicate: (MethodExecutor) -> Boolean) = this.apply {
        this.pausePredicates.add(predicate)
    }

    /**
     * Adds a terminate predicate to the execution.
     *
     * @param predicate Function1<MethodExecutor, Boolean>
     * @return MethodExecutor
     */
    fun terminateWhen(predicate: (MethodExecutor) -> Boolean) = this.apply {
        this.terminatePredicates.add(predicate)
    }

    /**
     * Runs this method execution. When and if the execution becomes
     * paused, the [pauseAction] consumer is invoked.
     *
     * During the pause action consumer, the execution can be hooked and un paused or
     * terminated if needed.
     *
     * @param pauseAction Function1<MethodExecutor, Unit>
     */
    fun execute(pauseAction: (MethodExecutor) -> Unit) {
        /*
         * To start the execution, we start in the unpaused state.
         */
        unpause()

        /*
         * Run the loop forever, we will break it during the pause logic.
         * However if the execution terminates we should break the loop no matter what.
         */
        while(!terminated) {

            /*
             * If the execution is paused.
             */
            if(isPaused()) {
                pauseAction(this)
                continue
            }

            /*
             * Step the execution forward to the next frame.
             */
            stepNext()
        }
    }

    /**
     * Steps the execution by a single frame.
     */
    fun stepNext() {
        /*
         * Terminate if we reach the end of the available instructions
         */
        if(currentIndex < method.instructions.size()) {
            terminated = true
            return
        }

        /*
         * Update all the current executor states to the next frame / next state.
         */
        currentInsn = method.instructions[++currentIndex]
        currentFrame = instructionFrames[currentIndex]
        currentExecutionState = ExecutionState(currentInsn, currentFrame)

        /*
         * Build and calculate the pushes / pops during this execution state.
         */
        currentExecutionState.pushes.addAll(this.buildPushedContexts())
        currentExecutionState.pops.addAll(this.buildPoppedContexts())

        /*
         * Record the current execution state.
         */
        states.add(currentExecutionState)

        /*
         * Terminate if any of the termination predicates match
         * the current execution state.
         */
        if(terminatePredicates.any { it(this) }) {
            terminated = true
        }

        /*
         * Pause the execution if any of the pause predicates match
         * the current execution state.
         */
        if(pausePredicates.any { it(this) }) {
            paused = true
        }
    }

    /**
     * Gets a list of stack contexts which were pushed in the current execution frame.
     *
     * @return List<StackContext>
     */
    private fun buildPushedContexts(): List<StackContext> {
        /*
         * If the current frame is a terminal frame, there will be nothing to push.
         */
        if(currentIndex >= instructionFrames.size - 1) return emptyList()

        /*
         * Make a clone of the stack values at the current and next
         * instruction frame.
         *
         * We are going to remove any matching from the next frame's cloned
         * stack to calculate the delta values pushed this frame.
         */
        val nextFrame = instructionFrames[currentIndex + 1]
        val nextStack = nextFrame.stack
        val currentStack = currentFrame.stack

        nextStack.removeAll(currentStack)

        /*
         * Update the executor's live stack with the delta's as
         * well as create [StackContext] objects for each pushed value.
         *
         * If the pushed value is reference to a previous pushed value such as a copy
         * of dup operation, we go ahead and grab it from the current stack and clone it.
         */
        return nextStack.map { StackContext(currentExecutionState, it) }.apply {
            this.forEach { stack.push(it) }
        }
    }

    /**
     * Calculates and builds a list of [StackContext] objects for any
     * values which have been popped from the stack in the current execution frame.
     *
     * @return List<StackContext>
     */
    private fun buildPoppedContexts(): List<StackContext> {
        /*
         * If the current frame is an entry point, there will be nothing to
         * pop from the stack.
         */
        if(currentIndex == 0) return emptyList()

        /*
         * Make a clone of the current and last frame's stacks.
         *
         * We will remove any matching elements from the last stack clone to
         * calculate the popped values in the current execution frame.
         */
        val lastFrame = instructionFrames[currentIndex - 1]
        val lastStack = lastFrame.stack
        val currentStack = currentFrame.stack

        lastStack.removeAll(currentStack)

        /*
         * If the current executor's stack is not empty,
         *
         * we want to use the previous [StackContext] which has been pushed
         * to the stack instead of creating a new StackContext.
         */
        return lastStack.map {
            if(stack.isNotEmpty()) stack.pop()
            else StackContext(currentExecutionState, it)
        }.apply {
            this.forEach { it.pops.add(currentExecutionState) }
        }
    }

    /**
     * Gets the stack of a instructional frame.
     */
    private val Frame<AbstractValue>.stack: MutableList<AbstractValue> get() {
        val ret = mutableListOf<AbstractValue>()
        for(i in 0 until this.stackSize) {
            ret.add(this.getStack(i))
        }
        return ret
    }
}