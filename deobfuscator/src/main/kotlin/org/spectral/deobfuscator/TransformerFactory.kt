package org.spectral.deobfuscator

import org.spectral.deobfuscator.transform.*
import org.spectral.deobfuscator.transform.controlflow.ControlFlowFixer
import org.spectral.deobfuscator.transform.euclidean.MultiplierRemover

/**
 * Builds and creates instances of [Transformer]s to initialize
 * in the deobfuscator to run.
 *
 * @property build Function0<Transformer>
 * @constructor
 */
enum class TransformerFactory(val order: Int, val build: () -> Transformer) {

    FIELD_INLINER(1, { FieldInliner() }),
    RUNTIME_EXCEPTION(2, { RuntimeExceptionRemover() }),
    CONTROL_FLOW(2, { ControlFlowFixer() }),
    OPAQUE_PREDICATE_CHECK(3, { OpaquePredicateCheckRemover() }),
    UNUSED_FIELD(4, { UnusedFieldRemover() }),
    ERROR_CONSTRUCTOR(5, { ErrorConstructorRemover() }),
    UNUSED_METHOD(6, { UnusedMethodRemover() }),
    OPAQUE_PREDICATE_ARG(7, { OpaquePredicateArgRemover() }),
    GOTO_REMOVER(8, { GotoRemover() }),
    REBUILD_FRAMES(9, { RebuildFrames() }),
    MULTIPLIER(10, { MultiplierRemover() });

    companion object {
        val values = enumValues<TransformerFactory>()
    }
}