package org.spectral.deobfuscator.transformer.controlflow

import org.objectweb.asm.tree.AbstractInsnNode.*
import org.objectweb.asm.tree.MethodNode
import org.objectweb.asm.tree.analysis.Analyzer
import org.objectweb.asm.tree.analysis.BasicInterpreter
import org.objectweb.asm.tree.analysis.BasicValue

/**
 * Analyzes the control-flow and builds a graph of [Blocks]
 *
 * @property blocks MutableList<Block>
 */
class ControlFlowAnalyzer : Analyzer<BasicValue>(BasicInterpreter()) {

    val blocks = mutableListOf<Block>()

    override fun init(owner: String, method: MethodNode) {
        val insns = method.instructions
        var currentBlock = Block()

        blocks.add(currentBlock)

        for(i in 0 until insns.size()) {
            val insn = insns[i]
            currentBlock.end++

            if(insn.next == null) break
            if(insn.next.type == LABEL ||
                    insn.type == JUMP_INSN ||
                    insn.type == LOOKUPSWITCH_INSN ||
                    insn.type == TABLESWITCH_INSN) {
                currentBlock = Block()
                currentBlock.start = i + 1
                currentBlock.end = i + 1
                blocks.add(currentBlock)
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

    private fun findBlock(index: Int): Block {
        return blocks.first { index in it.start until it.end }
    }
}