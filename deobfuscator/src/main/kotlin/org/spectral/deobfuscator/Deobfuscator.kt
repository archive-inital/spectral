package org.spectral.deobfuscator

import org.spectral.asm.core.ClassPool
import org.tinylog.kotlin.Logger

/**
 * Responsible for deobfuscating OSRS gamepacks to make them eaiser to read
 * as well as able to be decompiled.
 *
 * @property pool The class pool to deobfuscate
 * @constructor
 */
class Deobfuscator(val pool: ClassPool) {

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
        unregisteredTransformers.forEach {
            transformers.add(it.build())
            Logger.info("Registered transformer: '${it::class.java.simpleName}'")
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