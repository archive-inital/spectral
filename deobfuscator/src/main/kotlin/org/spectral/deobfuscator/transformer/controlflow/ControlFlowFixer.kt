package org.spectral.deobfuscator.transformer.controlflow

import org.objectweb.asm.tree.InsnList
import org.objectweb.asm.tree.LabelNode
import org.spectral.asm.ClassPool
import org.spectral.deobfuscator.Transformer
import org.spectral.deobfuscator.common.Transform
import org.tinylog.kotlin.Logger
import java.util.*
import kotlin.collections.AbstractMap

/**
 * Reorderes the instruction so that the control flow graph in the bytecode are
 * in a more logical order.
 */
@Transform(priority = 2)
class ControlFlowFixer : Transformer {

    private var counter = 0

    override fun transform(pool: ClassPool) {
        pool.classes.forEach { c ->
            c.methods.forEach { m ->
                /*
                 * Dealing with exceptions is a whole additional beast.
                 * So lets just skip them for now.
                 */
                if(m.tryCatchBlocks.isEmpty()) {
                    val cfg = ControlFlowAnalyzer()
                    cfg.analyze(c.name, m.node)

                    m.instructions = rebuildInstructions(m.instructions, cfg.blocks)

                    counter += cfg.blocks.size
                }
            }
        }

        Logger.info("Reordered $counter control-flow blocks.")
    }

    /**
     * Rebuilds the instruction list according to the order in which the blocks get executed.
     *
     * @param insns InsnList
     * @param blocks List<Block>
     * @return InsnList
     */
    private fun rebuildInstructions(insns: InsnList, blocks: List<Block>): InsnList {
        val newInsns = InsnList()

        if(blocks.isEmpty()) {
            return newInsns
        }

        val labelMap = LabelMap()
        val blockQueue = Collections.asLifoQueue(ArrayDeque<Block>())
        val movedBlocks = hashSetOf<Block>()

        blockQueue.add(blocks.first())

        while(blockQueue.isNotEmpty()) {
            val block = blockQueue.remove()

            if(block in movedBlocks) continue
            movedBlocks.add(block)

            block.branches.forEach { blockQueue.add(it.origin) }
            block.next?.let { blockQueue.add(it) }

            for(i in block.start until block.end) {
                newInsns.add(insns[i].clone(labelMap))
            }
        }

        return newInsns
    }

    /**
     * A virtual label map. If non exists at a JUMP, create a new one where it should be.
     *
     * @property map HashMap<LabelNode, LabelNode>
     * @property entries Nothing
     */
    private class LabelMap : AbstractMap<LabelNode, LabelNode>() {
        private val map = hashMapOf<LabelNode, LabelNode>()
        override val entries get() = throw IllegalAccessException()
        override fun get(key: LabelNode): LabelNode = map.getOrPut(key) { LabelNode() }
    }
}