package org.spectral.asm.code

import org.spectral.util.collection.asReadOnly

/**
 * Represents a collection of [Instruction] objects within a code block.
 *
 * @property code Code
 * @constructor
 */
class Instructions(val code: Code) : Iterable<Instruction> {

    private val instructions = mutableListOf<Instruction>()

    /**
     * The number of instruction in this collection.
     */
    val size: Int get() = instructions.size

    /**
     * Gets a read-only list of [Instruction] objects for this collections.
     *
     * @return List<Instruction>
     */
    fun asList(): List<Instruction> = instructions.asReadOnly()

    /**
     * Adds an [Instruction] at a specified instruction index.
     *
     * @param index Int
     * @param insn Instruction
     */
    fun addInstruction(index: Int, insn: Instruction) {
       instructions.add(index, insn)
    }

    /**
     * Regenerates the instruction pool for each instruction.
     */
    fun regeneratePool() {
        instructions.forEach { it.regeneratePool() }
    }

    override fun iterator(): Iterator<Instruction> {
        return this.instructions.iterator().asReadOnly()
    }
}