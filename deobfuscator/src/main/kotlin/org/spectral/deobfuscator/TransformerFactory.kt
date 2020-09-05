package org.spectral.deobfuscator

import org.spectral.deobfuscator.transform.RuntimeExceptionRemover

/**
 * Builds and creates instances of [Transformer]s to initialize
 * in the deobfuscator to run.
 *
 * @property build Function0<Transformer>
 * @constructor
 */
enum class TransformerFactory(val order: Int, val build: () -> Transformer) {

    RUNTIME_EXCEPTION(1, { RuntimeExceptionRemover() });

    companion object {
        val values = enumValues<TransformerFactory>()
    }
}