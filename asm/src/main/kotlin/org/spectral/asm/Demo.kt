package org.spectral.asm

import org.objectweb.asm.Opcodes.DUP
import org.spectral.asm.execution.MethodExecutor
import java.io.File

object Demo {

    @JvmStatic
    fun main(args: Array<String>) {
        val pool = ClassPool.loadJar(File("gamepack.jar"))

        val method = pool["client"]!!.methods.firstOrNull { it.name == "init" }!!

        val executor = MethodExecutor(method)

        executor
            .pauseWhen { it.currentInsn.opcode == DUP }
            .execute {
                println("Paused on DUP")
                it.unpause()
            }

        println()
    }
}