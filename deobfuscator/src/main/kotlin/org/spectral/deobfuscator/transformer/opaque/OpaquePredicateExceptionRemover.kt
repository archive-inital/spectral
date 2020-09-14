package org.spectral.deobfuscator.transformer.opaque

import org.objectweb.asm.Opcodes.*
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.IntInsnNode
import org.objectweb.asm.tree.JumpInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.spectral.asm.ClassPool
import org.spectral.asm.Method
import org.spectral.deobfuscator.Transformer
import org.spectral.deobfuscator.common.Transform
import java.lang.reflect.Modifier

/**
 * Removes the logic that checks the values passed in the opaque predicates in some
 * methods.
 *
 * E.g.
 *
 * if(var3 == -3409283420) {
 *      throw IllegalStateException()
 * }
 *
 * This transformer will leave the last arguments that are opaque predicates unused in the client
 * ass well as it will also leave dead code / methods which are no longer reachable.
 */
@Transform(priority = 2)
class OpaquePredicateExceptionRemover : Transformer {

    override fun transform(pool: ClassPool) {

    }

    /*
     * =======================================================
     * = UTILITY METHODS
     * =======================================================
     */



    /**
     * Gets the array index value of the last argument in a given method.
     *
     * The purpose of this method is because non static method push the LV
     * identified 'this' to the local variable table. Due to that, we need the
     * last index value to be one less for static methods.
     */
    private val Method.lastArgumentIndex: Int get() {
        val offset = if(Modifier.isStatic(this.access)) 1 else 0
        return (Type.getArgumentsAndReturnSizes(this.desc) shr 2) - offset - 1
    }

    /**
     * Gets whether an instruction pushes an Integer constant to the stack.
     */
    private val AbstractInsnNode.pushesIntValue: Boolean get() {
        return when(opcode) {
            LDC -> (this as LdcInsnNode).cst is Int
            in BIPUSH..SIPUSH, in ICONST_M1..ICONST_5 -> true
            else -> false
        }
    }

    /**
     * Gets whether the instruction is an if, or else instruction.
     */
    private val AbstractInsnNode.isIfStatement: Boolean get() {
        return this is JumpInsnNode && this.opcode != GOTO
    }

    /**
     * Gets whether the instruction is a return statement.
     */
    private val AbstractInsnNode.isReturnStatement: Boolean get() {
        return this.opcode in IRETURN..RETURN
    }

    /**
     * Gets the pushed INT value for a given instruction given the
     * opcode is actually pushing an INT constant.
     */
    private val AbstractInsnNode.pushedIntValue: Int get() {
        if(opcode in ICONST_M1..ICONST_5) return opcode - 3
        if(opcode == BIPUSH || opcode == SIPUSH) return (this as IntInsnNode).operand
        if(this is LdcInsnNode && this.cst is Int) return cst as Int
        throw IllegalArgumentException("The instruction opcode ${this.opcode} does push a constant integer to the stack.")
    }
}