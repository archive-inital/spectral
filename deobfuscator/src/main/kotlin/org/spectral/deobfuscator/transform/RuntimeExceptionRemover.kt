package org.spectral.deobfuscator.transform

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.tree.TryCatchBlockNode
import org.spectral.asm.core.ClassPool
import org.spectral.deobfuscator.Transformer
import org.spectral.deobfuscator.exception.DeobfuscatorException
import org.spectral.deobfuscator.util.nextPattern
import org.spectral.deobfuscator.util.opname
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

                /*
                 * Remove the try-catch blocks if the type is of
                 * the runtime exception internal name
                 */
                val blocks = m.node.tryCatchBlocks
                if(blocks.isNotEmpty()) {
                    val toRemove = mutableListOf<TryCatchBlockNode>()

                    blocks.forEach { block ->
                        if(block.type == null) {
                            toRemove.add(block)
                            counter++
                        }
                        else if(block.type == RUNTIME_EXCEPTION_NAME) {
                            val handler = block.handler.next.next
                            if(handler != null && handler.opcode == NEW) {
                                /*
                                 * Remove the try-catch handler instructions and add
                                 * the current [block] to the [toRemove] list for removal.
                                 */
                                handler.nextPattern(DUP, INVOKESPECIAL, LDC, INVOKEVIRTUAL,
                                    LDC, INVOKEVIRTUAL, INVOKEVIRTUAL, INVOKESTATIC, ATHROW)?.let {
                                    m.node.instructions.remove(handler.previous)
                                    m.node.instructions.remove(handler)
                                    it.forEach { m.node.instructions.remove(it) }
                                    toRemove.add(block)
                                    counter++

                                    if(it.last().next != null) {
                                        throw DeobfuscatorException("Did not remove all the handler instructions. $it")
                                    }
                                }
                            }
                        }
                    }

                    /*
                     * Remove all the try catch blocks.
                     */
                    m.node.tryCatchBlocks.removeAll(toRemove)
                }
            }
        }

        Logger.info("Removed $counter 'RuntimeException' try-catch blocks.")
    }

    companion object {
        /**
         * The runtime exception class internal name.
         */
        private const val RUNTIME_EXCEPTION_NAME = "java/lang/RuntimeException"
    }
}