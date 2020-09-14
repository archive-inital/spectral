package org.spectral.deobfuscator.transformer

import org.objectweb.asm.tree.FieldInsnNode
import org.spectral.asm.ClassPool
import org.spectral.deobfuscator.Transformer
import org.spectral.deobfuscator.common.Transform
import org.tinylog.kotlin.Logger
import java.lang.reflect.Modifier

/**
 * Removes fields where are not invoked in any methods.
 */
@Transform(priority = 6)
class UnusedFieldRemover : Transformer {

    override fun transform(pool: ClassPool) {
        var counter = 0

        val usedFields = pool.classes.flatMap { it.methods }
                .flatMap { it.instructions.toArray().asIterable() }
                .mapNotNull { it as? FieldInsnNode }
                .map { it.owner + "." + it.name }
                .toSet()

        pool.classes.forEach { c ->
            val fieldIterator = c.fields.iterator()
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