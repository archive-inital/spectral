package org.spectral.asm

import java.io.File

object Test {

    @JvmStatic
    fun main(args: Array<String>) {
        val pool = JarUtil.loadJar(File("gamepack.jar"))
        println()
    }
}