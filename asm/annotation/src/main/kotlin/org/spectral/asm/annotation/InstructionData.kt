package org.spectral.asm.annotation

@Target(AnnotationTarget.CLASS)
annotation class InstructionData(val opcode: Int, val name: String)