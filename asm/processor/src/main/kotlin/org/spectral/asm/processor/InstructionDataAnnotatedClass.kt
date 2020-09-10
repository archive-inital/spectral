package org.spectral.asm.processor

import org.spectral.asm.annotation.InstructionData
import javax.lang.model.element.TypeElement

class InstructionDataAnnotatedClass(internal val annotatedClassElement: TypeElement) {

    internal val opcode: Int
    internal val name: String

    init {
        val annotation = annotatedClassElement.getAnnotation(InstructionData::class.java)
        opcode = annotation.opcode
        name = annotation.name
    }
}