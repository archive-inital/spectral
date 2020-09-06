package org.spectral.deobfuscator

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import org.spectral.asm.core.ClassPool
import org.tinylog.kotlin.Logger

/**
 * The deobfuscator console command definition.
 */
class DeobfuscatorCommand : CliktCommand(
        name = "Deobfuscator",
        help = "Modifies the bytecode of a OSRS gamepack to make it more readable.",
        printHelpOnEmptyArgs = true,
        invokeWithoutSubcommand = true
) {

    /**
     * The input jar file to deobfuscate.
     */
    private val inputJarFile by argument("input jar", help = "Path to the input JAR file").file(mustExist = true, canBeDir = false)

    /**
     * The output jar file to export the deobfuscated classes to.
     */
    private val outputJarFile by argument("output jar", help = "Path to the output JAR file").file(mustExist = false, canBeDir = false)

    /**
     * Execute the command.
     */
    override fun run() {
        /*
         * Build the deobfuscator object.
         */

        val pool = ClassPool()
        pool.addArchive(inputJarFile)
        pool.init()

        val deobfuscator = Deobfuscator(pool)

        /*
         * Run the deobfuscator.
         */
        deobfuscator.run()

        /*
         * Export the modified classes.
         */

        Logger.info("Exporting deobfuscated classes to '${outputJarFile.path}'")

        if(outputJarFile.exists()) {
            Logger.info("Overwriting existing output JAR file: '${outputJarFile.path}'")
            outputJarFile.delete()
        }

        deobfuscator.pool.saveArchive(outputJarFile)

        Logger.info("Completed deobfuscation.")
    }
}