package org.spectral.deobfuscator

import org.objectweb.asm.ClassWriter
import org.spectral.asm.core.ClassPool
import org.tinylog.kotlin.Logger

/**
 * Responsible for deobfuscating OSRS gamepacks to make them eaiser to read
 * as well as able to be decompiled.
 *
 * @constructor
 */
class Deobfuscator private constructor() {

    /**
     * the class pool to deobfuscate.
     */
    lateinit var pool: ClassPool

    /**
     * Creates a deobfuscation initialized with a class pool.
     *
     * @param pool ClassPool
     * @constructor
     */
    constructor(pool: ClassPool) : this() {
        this.pool = pool.apply { this.values.removeIf { !it.real } }
    }

    /**
     * The transformers to be executed during the deobfuscation.
     * *NOTE* The order here matters.
     */
    private val transformers = mutableListOf<Transformer>()

    /**
     * Registers all the transformer types from the [TransformerFactory] enum
     * class.
     */
    private fun registerTransformers() {
        transformers.clear()

        Logger.info("Registering transformers")

        val unregisteredTransformers = TransformerFactory.values.apply { this.sortBy { it.order } }
        unregisteredTransformers.forEach { transformer ->
            transformers.add(transformer.build())
            Logger.info("Registered transformer: '${transformer.build()::class.java.simpleName}'")
        }

        Logger.info("Successfully registered ${transformers.size} bytecode transformers")
    }

    /**
     * Run the deobfuscator.
     */
    fun run() {
        Logger.info("Preparing deobfuscator...")

        /*
         * Register the transformers.
         */
        this.registerTransformers()

        /*
         * Run each transformer.
         */
        transformers.forEach {
            Logger.info("Running transformer: '${it::class.java.simpleName}'")
            it.execute(pool)
        }
    }

    companion object {
        /**
         * Static main method for the deobfuscator CLI usage.
         *
         * @param args Array<String>
         */
        @JvmStatic
        fun main(args: Array<String>) = DeobfuscatorCommand().main(args)
    }
}