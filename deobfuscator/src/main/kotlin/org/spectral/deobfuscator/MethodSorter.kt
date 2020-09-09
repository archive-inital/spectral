package org.spectral.deobfuscator

import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.MethodNode
import org.spectral.asm.core.ClassPool
import org.tinylog.kotlin.Logger
import java.lang.reflect.Modifier

/**
 * Sorts methods by the number of lines.
 */
class MethodSorter : Transformer {

    override fun execute(pool: ClassPool) {
        pool.values.forEach { c ->
            val methodsByLineCount = c.node.methods.associateWith { (it.firstLineIndex) ?: Integer.MAX_VALUE }

            val comparator = compareBy<MethodNode> { Modifier.isStatic(it.access) }.thenBy { methodsByLineCount.getValue(it) }
            c.node.methods = c.node.methods.sortedWith(comparator)
        }

        Logger.info("Sorted methods by number of lines in all classes.")
    }

    private val MethodNode.firstLineIndex: Int? get() {
        this.instructions.forEach { insn ->
            if(insn is LineNumberNode) {
                return insn.line
            }
        }

        return null
    }
}