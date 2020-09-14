package org.spectral.deobfuscator

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import io.github.classgraph.ClassGraph
import org.spectral.asm.ClassPool
import org.spectral.deobfuscator.common.Transform
import org.tinylog.kotlin.Logger
import java.io.File

/**
 * The Spectral Deobfuscator
 *
 * @property pool The loaded classes from a common class path to be deobfuscated.
 * @constructor
 */
class Deobfuscator(val pool: ClassPool) {

    /**
     * The loaded [Transformer] instances to run.
     *
     * The order in this list is the order they run in based on the priority given
     * in the annotation.
     */
    private val loadedTransformers = mutableListOf<Transformer>()

    /**
     * Creates a [Deobfuscator] object from a JAR file.
     *
     * @param jarFile File
     * @constructor
     */
    constructor(jarFile: File) : this(ClassPool.loadJar(jarFile))

    /**
     * Runs the deobfuscator.
     */
    fun run() {
        Logger.info("Preparing deobfuscator...")

        /*
         * Clear and reload all bytecode transformers.
         */
        loadedTransformers.clear()
        this.loadTransformers()

        Logger.info("Preparing to run bytecode transformers...")

        /*
         * Run all bytecode transformers.
         */

        loadedTransformers.forEach { transformer ->
            Logger.info("Running bytecode transformer: '${transformer::class.java.simpleName}'...")
            transformer.transform(pool)
        }

        Logger.info("Completed deobfuscation.")
    }

    private fun loadTransformers() {
        Logger.info("Scanning for transformer classess...")

        val scan = ClassGraph().enableAllInfo().acceptPackages("org.spectral.deobfuscator.transformer").scan()
        val scanResult = scan.getClassesWithAnnotation("org.spectral.deobfuscator.common.Transform")

        /*
         * Temporary map holding the transformer instance and its priority order.
         */
        val discoveredTransformers = mutableListOf<Pair<Int, Transformer>>()

        /*
         * Iterate all located transformer classes
         */
        scanResult.forEach { result ->
            val annotationInfo = result.getAnnotationInfo("org.spectral.deobfuscator.common.Transform")

            val priority = (annotationInfo.loadClassAndInstantiate() as Transform).priority

            @Suppress("UNCHECKED_CAST")
            val transformer = (result.loadClass() as Class<Transformer>).getDeclaredConstructor().newInstance()

            if(discoveredTransformers.any { it.first == priority }) {
                throw IllegalStateException("Unable to load transformer: '${transformer::class.java.simpleName}'. Priority level already exists.")
            }

            discoveredTransformers.add(priority to transformer)
        }

        /*
         * Sort the discovered transformers by priority.
         */
        discoveredTransformers.sortBy { it.first }
        loadedTransformers.addAll(discoveredTransformers.map { it.second })

        Logger.info("Discovered ${loadedTransformers.size} bytecode transformers.")
    }

    /**
     * Exports the deobfuscated classes to a given JAR file.
     *
     * @param jarFile File
     */
    fun export(jarFile: File) {
       Logger.info("Exporting deobfuscated classes to JAR file: '${jarFile.path}'.")
        pool.saveJar(jarFile)
    }

    companion object {

        /**
         * Main Static Method for the Deobfuscator.
         *
         * @param args Array<String>
         * @return CliktCommand
         */
        @JvmStatic
        fun main(args: Array<String>) = object : CliktCommand(
            name = "Deobfuscator",
            help = "Deobfuscates the OSRS gamepack bytecode.",
            invokeWithoutSubcommand = true,
            printHelpOnEmptyArgs = true
        ) {

            /**
             * The input JAR File path.
             */
            private val inputJarFile by argument("input", help = "The input JAR file path.").file(mustExist = true, canBeDir = false)

            /**
             * The output JAR File path.
             */
            private val outputJarFile by argument("output", help = "The output JAR file path.").file(mustExist = false, canBeDir = false)

            /**
             * Runs the command logic.
             */
            override fun run() {
                /*
                 * If the output JAR File already exists, delete it so we
                 * can overwrite it.
                 */
                if(outputJarFile.exists()) {
                    Logger.info("Output JAR file: '${outputJarFile.path}' already exists. Deleting it...")
                    outputJarFile.delete()
                }

                /*
                 * Create the Deobfuscator object.
                 */
                val deobfuscator = Deobfuscator(inputJarFile)

                /*
                 * Run the deobfuscator
                 */
                deobfuscator.run()
            }
        }.main(args)
    }
}