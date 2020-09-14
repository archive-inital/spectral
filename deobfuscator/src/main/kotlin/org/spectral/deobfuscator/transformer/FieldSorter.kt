package org.spectral.deobfuscator.transformer

import org.objectweb.asm.Type
import org.spectral.asm.ClassPool
import org.spectral.asm.Field
import org.spectral.deobfuscator.Transformer
import org.spectral.deobfuscator.common.Transform
import org.tinylog.kotlin.Logger
import java.lang.reflect.Modifier

/**
 * Sorts the fields in each class based on comparator specs
 */
@Transform(priority = 11)
class FieldSorter : Transformer {

    override fun transform(pool: ClassPool) {
        pool.classes.forEach { c ->
            c.fields = c.fields.sortedWith(FIELD_COMPARATOR).toMutableList()
        }

        Logger.info("Re-ordered non-static fields within classes.")
    }

    private val FIELD_COMPARATOR: Comparator<Field> = compareBy<Field> { !Modifier.isStatic(it.access) }
            .thenBy { Modifier.toString(it.access and Modifier.fieldModifiers()) }
            .thenBy { Type.getType(it.desc).className }
            .thenBy { it.name }
}