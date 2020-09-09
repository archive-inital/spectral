package org.spectral.deobfuscator.transform

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import org.spectral.asm.core.ClassPool
import org.spectral.deobfuscator.Transformer
import org.tinylog.kotlin.Logger
import java.lang.reflect.Modifier
import java.util.*

/**
 * Removes the check for the last parameter primitive constant. If the garbage value
 * is removed or changed an [IllegalStateException] is thrown.
 *
 * This transformer removes the check and the exception code.
 */
class OpaquePredicateCheckRemover : Transformer {

    override fun execute(pool: ClassPool) {
        var counter = 0
        val opaqueValues = TreeMap<String, Int>()

        /**
         * Loop through each method, in each class.
         */
        pool.values.forEach classLoop@ { c ->
            c.node.methods.forEach methodLoop@ { m ->
                val insns = m.instructions.iterator()
                val lastParamIndex = m.lastParamIndex

                /**
                 * Loop through each instruction inside of
                 * the method.
                 */
                while(insns.hasNext()) {
                    val insn = insns.next()

                    /**
                     * The number of instructions to delete to remove
                     * the predicate check.
                     */
                    val deleteInsnCount = if(insn.matchExceptionPattern(lastParamIndex)) {
                        7
                    } else if(insn.matchesReturnPattern(lastParamIndex)) {
                        4
                    } else {
                        continue
                    }

                    val pushedConstant = insn.next.intValue
                    val predicateOpcode = insn.next.next.opcode

                    val label = (insn.next.next as JumpInsnNode).label.label

                    /**
                     * Remove the ILOAD current instruction.
                     */
                    insns.remove()

                    /**
                     * Repeat for the remaining instructions of the check.
                     */
                    repeat(deleteInsnCount - 1) {
                        insns.next()
                        insns.remove()
                        counter++
                    }

                    insns.add(JumpInsnNode(Opcodes.GOTO, LabelNode(label)))
                    opaqueValues["${c.name}.${m.name}${dropLastArg(m.desc)}"] = passingVal(pushedConstant, predicateOpcode)
                }
            }
        }

        Logger.info("Removed $counter opaque predicate garbage value checks.")
    }

    /**
     * Gets the index of the last parameter in the method.
     */
    private val MethodNode.lastParamIndex: Int get() {
        val offset = if(Modifier.isStatic(access)) 1 else 0
        return (Type.getArgumentsAndReturnSizes(desc) shr 2) - offset - 1
    }

    /**
     * Checks if the instruction matches the predicate check instruction pattern.
     *
     * @receiver AbstractInsnNode
     * @return Boolean
     */
    private fun AbstractInsnNode.matchExceptionPattern(paramIndex: Int): Boolean {
        val i0 = this
        if(i0.opcode != Opcodes.ILOAD) return false
        i0 as VarInsnNode

        if(i0.`var` != paramIndex) return false

        val i1 = i0.next
        if(!i1.isIntValue()) return false

        val i2 = i1.next
        if(!i2.isIf()) return false

        val i3 = i2.next
        if(i3.opcode != Opcodes.NEW) return false

        val i4 = i3.next
        if(i4.opcode != Opcodes.DUP) return false

        val i5 = i4.next
        if(i5.opcode != Opcodes.INVOKESPECIAL) return false
        i5 as MethodInsnNode
        if(i5.owner != Type.getInternalName(IllegalStateException::class.java)) return false

        val i6 = i5.next
        if(i6.opcode != Opcodes.ATHROW) return false

        return true
    }

    /**
     * Checks if the current instruction matches the return
     * opaque predicate check pattern
     *
     * @receiver AbstractInsnNode
     * @param paramIndex Int
     * @return Boolean
     */
    private fun AbstractInsnNode.matchesReturnPattern(paramIndex: Int): Boolean {
        val i0 = this
        if(i0.opcode != Opcodes.ILOAD) return false
        i0 as VarInsnNode
        if(i0.`var` != paramIndex) return false

        val i1 = i0.next
        if(!i1.isIntValue()) return false

        val i2 = i1.next
        if(!i2.isIf()) return false

        val i3 = i2.next
        if(!i3.isReturn()) return false

        return true
    }

    /**
     * Whether a given instruction is pushing an [Int] to the stack.
     *
     * @receiver AbstractInsnNode
     * @return Boolean
     */
    private fun AbstractInsnNode.isIntValue(): Boolean {
        return when(opcode) {
            Opcodes.LDC -> (this as LdcInsnNode).cst is Int
            Opcodes.SIPUSH, Opcodes.BIPUSH, Opcodes.ICONST_0, Opcodes.ICONST_1, Opcodes.ICONST_2, Opcodes.ICONST_3, Opcodes.ICONST_4, Opcodes.ICONST_5, Opcodes.ICONST_M1 -> true
            else -> false
        }
    }

    /**
     * Whether a given instruction is an IF statement.
     *
     * @receiver AbstractInsnNode
     * @return Boolean
     */
    private fun AbstractInsnNode.isIf(): Boolean {
        return this is JumpInsnNode && this.opcode != Opcodes.GOTO
    }

    private fun AbstractInsnNode.isReturn(): Boolean {
        return when(opcode) {
            Opcodes.RETURN, Opcodes.ARETURN, Opcodes.DRETURN, Opcodes.FRETURN, Opcodes.IRETURN, Opcodes.LRETURN -> true
            else -> false
        }
    }

    private fun passingVal(pushed: Int, ifOpcode: Int): Int {
        return when(ifOpcode) {
            Opcodes.IF_ICMPEQ -> pushed
            Opcodes.IF_ICMPGE,
            Opcodes.IF_ICMPGT -> pushed + 1
            Opcodes.IF_ICMPLE,
            Opcodes.IF_ICMPLT,
            Opcodes.IF_ICMPNE -> pushed - 1
            else -> error(ifOpcode)
        }
    }

    val AbstractInsnNode.intValue: Int get() {
        if (opcode in 2..8) return opcode - 3
        if (opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH) return (this as IntInsnNode).operand
        if (this is LdcInsnNode && cst is Int) return cst as Int
        error(this)
    }

    private fun dropLastArg(desc: String): String {
        val type = Type.getMethodType(desc)
        return Type.getMethodDescriptor(type.returnType, *type.argumentTypes.copyOf(type.argumentTypes.size - 1))
    }
}