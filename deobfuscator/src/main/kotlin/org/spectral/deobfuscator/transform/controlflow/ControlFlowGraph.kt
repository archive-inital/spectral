package org.spectral.deobfuscator.transform.controlflow

import org.objectweb.asm.tree.AbstractInsnNode.JUMP_INSN
import org.objectweb.asm.tree.AbstractInsnNode.LABEL
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicInterpreter
import org.objectweb.asm.tree.analysis.BasicValue

/**
 * An ASM analyzer for building a chain of [Block] elements
 * which represent a control flow graph.
 */
class ControlFlowGraph : Analyzer<BasicValue>(BasicInterpreter()) {

    /**
     * The chain of [Block]s in this control flow graph.
     */
    val blocks = mutableListOf<Block>()

    override fun init(owner: String, method: MethodNode) {
        val insns = method.instructions
        var current = Block()
        blocks.add(current)

        for(i in 0 until insns.size()) {
            val insn = insns[i]

            current.end++

            if(insn.next == null) break
            if(insn.next.type == LABEL ||
                    insn.type == JUMP_INSN ||
                    insn.type == LOOKUPSWITCH ||
                    insn.type == TABLESWITCH) {
                /*
                 * Create a new block
                 */
                current = Block()
                current.start = i + 1
                current.end = i + 1
                blocks.add(current)
            }
        }
    }

    override fun newControlFlowEdge(insnIndex: Int, successorIndex: Int) {
        val currentBlock = findBlock(insnIndex)
        val nextBlock = findBlock(successorIndex)

        if(currentBlock != nextBlock) {
            if(insnIndex + 1 == successorIndex) {
                currentBlock.next = nextBlock
                nextBlock.prev = currentBlock
            } else {
                currentBlock.branches.add(nextBlock)
            }
        }
    }

    /**
     * Gets the the control flow block that the given instruction index
     * resides in.
     *
     * @param insnIndex Int
     * @return Block
     */
    private fun findBlock(insnIndex: Int): Block {
        return blocks.first { insnIndex in it.start until it.end }
    }
}