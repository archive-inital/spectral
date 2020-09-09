package org.spectral.deobfuscator

import org.spectral.deobfuscator.transform.*
import org.spectral.deobfuscator.transform.controlflow.ControlFlowFixer

/**
 * Builds and creates instances of [Transformer]s to initialize
 * in the deobfuscator to run.
 *
 * @property build Function0<Transformer>
 * @constructor
 */
enum class TransformerFactory(val order: Int, val build: () -> Transformer) {

    FIELD_INLINER(1, { FieldInliner() }),
    TRY_CATCH_REMOVER(2, { TryCatchRemover() }),
    DEAD_CODE(3, { DeadCodeRemover() }),
    CONTROL_FLOW(4, { ControlFlowFixer() }),
    OPAQUE_PREDICATE_CHECK(5, { OpaquePredicateCheckRemover() }),
    UNUSED_FIELD(6, { UnusedFieldRemover() }),
    ERROR_CONSTRUCTOR(7, { ErrorConstructorRemover() }),
    UNUSED_METHOD(8, { UnusedMethodRemover() }),
    //OPAQUE_PREDICATE_ARG(9, { OpaquePredicateArgRemover() }),
    GOTO_REMOVER(10, { GotoRemover() }),
    DUPLICATE_METHODS(11, { DuplicateMethodRemover() }),
    FIELD_SORTER(12, { FieldSorter() }),
    METHOD_SORTER(13, { MethodSorter() });
    //REBUILD_FRAMES(14, { RebuildFrames() }),
    //MULTIPLIER(15, { MultiplierRemover() });

    companion object {
        val values = enumValues<TransformerFactory>()
    }
}