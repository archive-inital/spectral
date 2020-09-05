package org.spectral.deobfuscator.transform

import org.objectweb.asm.Type
import org.spectral.asm.core.ClassPool
import org.spectral.deobfuscator.Transformer
import org.tinylog.kotlin.Logger
import java.lang.RuntimeException

/**
 * Removes the [RuntimeException] catch blocks from methods.
 */
class RuntimeExceptionRemover : Transformer {

    private var counter = 0

    override fun execute(pool: ClassPool) {
        pool.forEach { c ->
            c.methods.forEach methodLoop@ { m ->
                /*
                 * Skip the client.init()V method.
                 */
                if(c.name == "client" && m.name == "init" && m.desc == "()V") {
                    return@methodLoop
                }

                val oldSize = m.node.tryCatchBlocks.size

                /*
                 * Remove the try-catch blocks if the type is of
                 * the runtime exception internal name
                 */
                m.node.tryCatchBlocks.removeIf { it.type == RUNTIME_EXCEPTION_NAME }
                counter += oldSize - m.node.tryCatchBlocks.size
            }
        }

        Logger.info("Removed $counter 'RuntimeException' try-catch blocks.")
    }

    companion object {
        /**
         * The runtime exception class internal name.
         */
        private val RUNTIME_EXCEPTION_NAME = Type.getInternalName(RuntimeException::class.java)
    }
}