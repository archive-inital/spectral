package org.spectral.asm.visitor

import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ASM8
import org.spectral.asm.Class
import org.spectral.asm.Method
import org.spectral.asm.code.*
import org.spectral.asm.signature.Signature

/**
 * Represents an implementation of the ASM [MethodVisitor] for creating methods.
 *
 * @property cls Class
 * @constructor
 */
class AsmMethodVisitor(
    val cls: Class,
    access: Int,
    name: String,
    signature: Signature,
    exceptions: Array<String>?
) : MethodVisitor(ASM8) {

    /**
     * The method we are creating and visiting.
     */
    val method = Method(cls, name, signature)

    /**
     * The code block of the method.
     */
    lateinit var code: Code

    init {
        method.access = access

        if(exceptions != null) {
            for(e in exceptions) {
                method.exceptions.addException(e)
            }
        }
    }

    override fun visitCode() {
        code = Code(method)
    }

    private fun createInstruction(opcode: Int): Instruction? {
        val type = InstructionType.values().firstOrNull { it.opcode == opcode }
        if(type != null) {
            val constructor = type.insnClass.java.getDeclaredConstructor(Instructions::class.java, InstructionType::class.java)
            val insn: Instruction = constructor.newInstance(code.instructions, type) as Instruction

            return insn
        }

        return null
    }

    override fun visitInsn(opcode: Int) {
        val insn = when {
            else -> createInstruction(opcode)
        }

        if(insn != null) {
            code.instructions.addInstruction(insn)
        }
    }

    override fun visitTryCatchBlock(start: Label, end: Label, handler: Label, type: String?) {
        val exceptions = code.exceptions
        val e = Exception(exceptions)

        val insns = code.instructions

        val startLabel = insns.findOrCreateLabel(start)
        val endLabel = insns.findOrCreateLabel(end)
        val handlerLabel = insns.findOrCreateLabel(handler)

        e.start = startLabel
        e.end = endLabel
        e.handler = handlerLabel

        if(type != null) {
            e.catchType = type
        }

        exceptions.add(e)
    }

    override fun visitLineNumber(line: Int, start: Label) {
        code.instructions.findLabel(start)?.let {
            it.lineNumber = line
        }
    }

    override fun visitEnd() {
        if(::code.isInitialized) {
            method.code = code
        }

        cls.methods.add(method)
    }
}