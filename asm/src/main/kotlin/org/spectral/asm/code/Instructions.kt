package org.spectral.asm.code

import org.spectral.asm.code.instruction.Label
import org.spectral.util.collection.asReadOnly

/**
 * Represents a collection of [Instruction] objects within a code block.
 *
 * @property code Code
 * @constructor
 */
class Instructions(val code: Code) : Iterable<Instruction> {

    private val instructions = mutableListOf<Instruction>()
    private val labelMap = hashMapOf<org.objectweb.asm.Label, Label>()

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

    fun createLabelFor(target: Instruction): Label = createLabelFor(target, false)

    fun createLabelFor(target: Instruction, forceCreate: Boolean): Label {
       if(target is Label) {
           return target
       }

        val i = instructions.indexOf(target)
        if(i > 0) {
            val prev = instructions[i - 1]

            if(!forceCreate && prev is Label) {
                return prev
            }
        }

        val label = Label(this)
        label.label = org.objectweb.asm.Label()
        instructions.add(i, label)
        return label
    }

    fun findLabel(target: org.objectweb.asm.Label): Label? {
        return labelMap[target]
    }

    fun findOrCreateLabel(target: org.objectweb.asm.Label): Label {
        labelMap[target]?.let {
            return it
        }

        val label = Label(this, target)
        labelMap[target] = label

        return label
    }

    fun rebuildLabels() {
        labelMap.clear()

        instructions.forEach { insn ->
            if(insn is Label) {
                val label = org.objectweb.asm.Label()
                insn.label = label

                labelMap[label] = insn
            }
        }
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