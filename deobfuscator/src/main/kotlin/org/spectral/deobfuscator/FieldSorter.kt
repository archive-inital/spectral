package org.spectral.deobfuscator

import org.objectweb.asm.Type
import org.objectweb.asm.tree.FieldNode
import org.spectral.asm.core.ClassPool
import org.tinylog.kotlin.Logger
import java.lang.reflect.Modifier

/**
 * Sorts the fields in each class based on comparator specs
 */
class FieldSorter : Transformer {

    override fun execute(pool: ClassPool) {
        pool.values.forEach { c ->
            c.node.fields = c.node.fields.sortedWith(FIELD_COMPARATOR)
        }

        Logger.info("Re-ordered non-static fields within classes.")
    }

    private val FIELD_COMPARATOR: Comparator<FieldNode> = compareBy<FieldNode> { !Modifier.isStatic(it.access) }
            .thenBy { Modifier.toString(it.access and Modifier.fieldModifiers()) }
            .thenBy { Type.getType(it.desc).className }
            .thenBy { it.name }
}