package org.spectral.deobfuscator.transform

import org.objectweb.asm.tree.FieldInsnNode
import org.spectral.asm.core.ClassPool
import org.spectral.deobfuscator.Transformer
import org.tinylog.kotlin.Logger
import java.lang.reflect.Modifier

/**
 * Removes fields where are not invoked in any methods.
 */
class UnusedFieldRemover : Transformer {

    override fun execute(pool: ClassPool) {
        var counter = 0

        val usedFields = pool.values.flatMap { it.methods }
                .flatMap { it.instructions.toArray().asIterable() }
                .mapNotNull { it as? FieldInsnNode }
                .map { it.owner + "." + it.name }
                .toSet()

        pool.values.forEach { c ->
            val fieldIterator = c.node.fields.iterator()
            while(fieldIterator.hasNext()) {
                val field = fieldIterator.next()
                val fName = c.name + "." + field.name
                if(!usedFields.contains(fName) && Modifier.isFinal(field.access)) {
                    fieldIterator.remove()
                    counter++
                }
            }
        }

        Logger.info("Removed $counter unused fields.")
    }
}