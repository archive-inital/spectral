package org.spectral.asm

import java.io.File

object Demo {

    @JvmStatic
    fun main(args: Array<String>) {
        val pool = ClassPool.loadJar(File("gamepack.jar"))
        println()
        pool.saveJar(File("gamepack-out.jar"))
    }
}