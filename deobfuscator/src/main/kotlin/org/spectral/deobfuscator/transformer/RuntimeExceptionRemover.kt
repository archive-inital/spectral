package org.spectral.deobfuscator.transformer

import org.objectweb.asm.Type
import org.spectral.asm.ClassPool
import org.spectral.deobfuscator.Transformer
import org.spectral.deobfuscator.common.Transform
import org.tinylog.kotlin.Logger
import java.lang.RuntimeException

/**
 * Removes the junk [RuntimeException]s from each method in every class within
 * the class pool
 */
@Transform(priority = 1)
class RuntimeExceptionRemover : Transformer {

    private var counter = 0

    override fun transform(pool: ClassPool) {
        pool.classes.forEach classLoop@ { c ->
            c.methods.forEach methodLoop@ { m ->
                val size = m.tryCatchBlocks.size
                m.tryCatchBlocks.removeIf { it.type == Type.getInternalName(RuntimeException::class.java) }
                counter += size - m.tryCatchBlocks.size
            }
        }

        Logger.info("Removed $counter RuntimeException try-catch blocks.")
    }
}