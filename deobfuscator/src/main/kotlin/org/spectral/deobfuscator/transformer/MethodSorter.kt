package org.spectral.deobfuscator.transformer

import org.objectweb.asm.tree.LineNumberNode
import org.spectral.asm.ClassPool
import org.spectral.asm.Method
import org.spectral.deobfuscator.Transformer
import org.spectral.deobfuscator.common.Transform
import org.tinylog.kotlin.Logger
import java.lang.reflect.Modifier

/**
 * Sorts methods by the number of lines.
 */
@Transform(priority = 12)
class MethodSorter : Transformer {

    override fun transform(pool: ClassPool) {
        pool.classes.forEach { c ->
            val methodsByLineCount = c.methods.associateWith { (it.firstLineIndex) ?: Integer.MAX_VALUE }

            val comparator = compareBy<Method> { Modifier.isStatic(it.access) }.thenBy { methodsByLineCount.getValue(it) }
            c.methods = c.methods.sortedWith(comparator).toMutableList()
        }

        Logger.info("Sorted methods by number of lines in all classes.")
    }

    private val Method.firstLineIndex: Int? get() {
        this.instructions.forEach { insn ->
            if(insn is LineNumberNode) {
                return insn.line
            }
        }

        return null
    }
}