package org.spectral.deobfuscator

import org.objectweb.asm.Type
import org.spectral.asm.core.ClassPool
import org.tinylog.kotlin.Logger
import java.lang.RuntimeException

/**
 * Removes the try-catch blocks which throw a [RuntimeException] error
 * based on method and field names.
 */
class TryCatchRemover : Transformer {

    override fun execute(pool: ClassPool) {
        var counter = 0

        pool.values.forEach { c ->
            c.node.methods.forEach methodLoop@ { m ->
                val size = m.tryCatchBlocks.size
                m.tryCatchBlocks.removeIf { it.type == Type.getInternalName(RuntimeException::class.java) }
                counter += size - m.tryCatchBlocks.size
            }
        }

        Logger.info("Removed $counter RuntimeException try-catch blocks.")
    }
}