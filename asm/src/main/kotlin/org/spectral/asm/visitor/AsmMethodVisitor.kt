package org.spectral.asm.visitor

import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ASM8
import org.spectral.asm.Class
import org.spectral.asm.Method
import org.spectral.asm.code.Code
import org.spectral.asm.code.Exception
import org.spectral.asm.code.Instruction
import org.spectral.asm.code.InstructionType
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

    override fun visitEnd() {
        if(::code.isInitialized) {
            method.code = code
        }

        cls.methods.add(method)
    }
}