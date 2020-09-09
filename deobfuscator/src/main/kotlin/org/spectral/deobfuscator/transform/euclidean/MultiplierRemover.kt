package org.spectral.deobfuscator.transform.euclidean

import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import org.objectweb.asm.tree.analysis.*
import org.spectral.asm.core.ClassPool
import org.spectral.deobfuscator.Transformer
import org.tinylog.kotlin.Logger

/**
 * Responsible for removing primitive field multipliers.
 *
 * Credits for this transformer goes to RuneStar.
 * I couldn't be bothered to deal with this headache
 */
class MultiplierRemover : Transformer {

    override fun execute(pool: ClassPool) {
        val multipliers = MultiplierFinder().getMultipliers(pool)

        Logger.info("Found ${multipliers.size} multipliers.")
        Logger.info("Removing multipliers...")

        pool.values.forEach { c ->
            c.node.methods.forEach { m ->
                m.maxStack += 2
                cancelOutMultipliers(m, decoders = multipliers)
                solveConstantMath(c.node, m)
                m.maxStack -= 2
            }
        }

        Logger.info("Completed removal of ${multipliers.size} multipliers.")
    }


    private fun cancelOutMultipliers(m: MethodNode, decoders: Map<String, Long>) {
        val insnList = m.instructions
        for (insn in insnList.iterator()) {
            if (insn !is FieldInsnNode) continue
            if (insn.desc != Type.INT_TYPE.descriptor && insn.desc != Type.LONG_TYPE.descriptor) continue
            val fieldName = "${insn.owner}.${insn.name}"
            val decoder = decoders[fieldName] ?: continue
            when (insn.opcode) {
                Opcodes.GETFIELD, Opcodes.GETSTATIC -> {
                    when (insn.desc) {
                        Type.INT_TYPE.descriptor -> {
                            when (insn.next.opcode) {
                                Opcodes.I2L -> insnList.insertSafe(insn.next, LdcInsnNode(invert(decoder)), InsnNode(
                                        Opcodes.LMUL
                                )
                                )
                                else -> insnList.insertSafe(insn, LdcInsnNode(invert(decoder.toInt())), InsnNode(Opcodes.IMUL))
                            }
                        }
                        Type.LONG_TYPE.descriptor -> insnList.insertSafe(insn, LdcInsnNode(invert(decoder)), InsnNode(
                                Opcodes.LMUL
                        )
                        )
                        else -> error(insn)
                    }
                }
                Opcodes.PUTFIELD -> {
                    when (insn.desc) {
                        Type.INT_TYPE.descriptor -> {
                            when (insn.previous.opcode) {
                                Opcodes.DUP_X1 -> {
                                    insnList.insertBeforeSafe(insn.previous, LdcInsnNode(decoder.toInt()), InsnNode(
                                            Opcodes.IMUL
                                    )
                                    )
                                    insnList.insertSafe(insn, LdcInsnNode(invert(decoder.toInt())), InsnNode(Opcodes.IMUL))
                                }
                                Opcodes.DUP, Opcodes.DUP_X2, Opcodes.DUP2, Opcodes.DUP2_X1, Opcodes.DUP2_X2 -> error(insn)
                                else -> insnList.insertBeforeSafe(insn, LdcInsnNode(decoder.toInt()), InsnNode(Opcodes.IMUL))
                            }
                        }
                        Type.LONG_TYPE.descriptor -> {
                            when (insn.previous.opcode) {
                                Opcodes.DUP2_X1 -> {
                                    insnList.insertBeforeSafe(insn.previous, LdcInsnNode(decoder), InsnNode(Opcodes.LMUL))
                                    insnList.insertSafe(insn, LdcInsnNode(invert(decoder)), InsnNode(Opcodes.LMUL))
                                }
                                Opcodes.DUP, Opcodes.DUP_X1, Opcodes.DUP_X2, Opcodes.DUP2, Opcodes.DUP2_X2 -> error(insn)
                                else -> insnList.insertBeforeSafe(insn, LdcInsnNode(decoder), InsnNode(Opcodes.LMUL))
                            }
                        }
                        else -> error(insn)
                    }
                }
                Opcodes.PUTSTATIC -> {
                    when (insn.desc) {
                        Type.INT_TYPE.descriptor -> {
                            when (insn.previous.opcode) {
                                Opcodes.DUP -> {
                                    insnList.insertBeforeSafe(insn.previous, LdcInsnNode(decoder.toInt()), InsnNode(
                                            Opcodes.IMUL
                                    )
                                    )
                                    insnList.insertSafe(insn, LdcInsnNode(invert(decoder.toInt())), InsnNode(Opcodes.IMUL))
                                }
                                Opcodes.DUP_X1, Opcodes.DUP_X2, Opcodes.DUP2, Opcodes.DUP2_X1, Opcodes.DUP2_X2 -> error(insn)
                                else -> insnList.insertBeforeSafe(insn, LdcInsnNode(decoder.toInt()), InsnNode(Opcodes.IMUL))
                            }
                        }
                        Type.LONG_TYPE.descriptor -> {
                            when (insn.previous.opcode) {
                                Opcodes.DUP2 -> {
                                    insnList.insertBeforeSafe(insn.previous, LdcInsnNode(decoder), InsnNode(Opcodes.LMUL))
                                    insnList.insertSafe(insn, LdcInsnNode(invert(decoder)), InsnNode(Opcodes.LMUL))
                                }
                                Opcodes.DUP, Opcodes.DUP_X1, Opcodes.DUP_X2, Opcodes.DUP2_X1, Opcodes.DUP2_X2 -> error(insn)
                                else -> insnList.insertBeforeSafe(insn, LdcInsnNode(decoder), InsnNode(Opcodes.LMUL))
                            }
                        }
                        else -> error(insn)
                    }
                }
            }
        }
    }

    private fun solveConstantMath(c: ClassNode, m: MethodNode) {
        val insnList = m.instructions
        val interpreter = Inter()
        val analyzer = Analyzer(interpreter)
        analyzer.analyze(c.name, m)
        for (mul in interpreter.constantMultiplications) {
            when (mul.insn.opcode) {
                Opcodes.IMUL -> associateMultiplication(insnList, mul, 1)
                Opcodes.LMUL -> associateMultiplication(insnList, mul, 1L)
                else -> error(mul)
            }
        }
    }

    private fun associateMultiplication(insnList: InsnList, mul: Expr.Mul, num: Int) {
        val n = num * mul.const.n.toInt()
        val other = mul.other
        when {
            other is Expr.Mul -> {
                insnList.removeSafe(mul.insn, mul.const.insn)
                associateMultiplication(insnList, other, n)
            }
            other is Expr.Const -> {
                insnList.removeSafe(mul.insn, mul.const.insn)
                insnList.setSafe(other.insn, loadInt(n * other.n.toInt()))
            }
            other is Expr.Add -> {
                insnList.removeSafe(mul.insn, mul.const.insn)
                distributeAddition(insnList, other.a, n)
                distributeAddition(insnList, other.b, n)
            }
            n == 1 -> insnList.removeSafe(mul.insn, mul.const.insn)
            else -> insnList.setSafe(mul.const.insn, loadInt(n))
        }
    }

    private fun associateMultiplication(insnList: InsnList, mul: Expr.Mul, num: Long) {
        val n = num * mul.const.n.toLong()
        val other = mul.other
        when {
            other is Expr.Mul -> {
                insnList.removeSafe(mul.insn, mul.const.insn)
                associateMultiplication(insnList, other, n)
            }
            other is Expr.Const -> {
                insnList.removeSafe(mul.insn, mul.const.insn)
                insnList.setSafe(other.insn, loadLong(n * other.n.toLong()))
            }
            other is Expr.Add -> {
                insnList.removeSafe(mul.insn, mul.const.insn)
                distributeAddition(insnList, other.a, n)
                distributeAddition(insnList, other.b, n)
            }
            n == 1L -> insnList.removeSafe(mul.insn, mul.const.insn)
            else -> insnList.setSafe(mul.const.insn, loadLong(n))
        }
    }

    private fun distributeAddition(insnList: InsnList, expr: Expr, n: Int) {
        when (expr) {
            is Expr.Const -> insnList.setSafe(expr.insn, loadInt(n * expr.n.toInt()))
            is Expr.Mul -> associateMultiplication(insnList, expr, n)
            else -> error(expr)
        }
    }

    private fun distributeAddition(insnList: InsnList, expr: Expr, n: Long) {
        when (expr) {
            is Expr.Const -> insnList.setSafe(expr.insn, loadLong(n * expr.n.toLong()))
            is Expr.Mul -> associateMultiplication(insnList, expr, n)
            else -> error(expr)
        }
    }

    private class Inter : Interpreter<Expr>(Opcodes.ASM8) {

        private val sourceInterpreter = SourceInterpreter()

        private val mults = LinkedHashMap<AbstractInsnNode, Expr.Mul>()

        override fun binaryOperation(insn: AbstractInsnNode, value1: Expr, value2: Expr): Expr? {
            val bv = sourceInterpreter.binaryOperation(insn, value1.sv, value2.sv) ?: return null
            if (value1 == value2) return Expr.Var(bv)
            return when (insn.opcode) {
                Opcodes.IMUL, Opcodes.LMUL -> {
                    if (value1 !is Expr.Const && value2 !is Expr.Const) {
                        Expr.Var(bv)
                    } else {
                        Expr.Mul(bv, value1, value2).also {
                            mults[insn] = it
                        }
                    }
                }
                Opcodes.IADD, Opcodes.ISUB, Opcodes.LADD, Opcodes.LSUB -> {
                    if ((value1 is Expr.Const || value1 is Expr.Mul) && (value2 is Expr.Const || value2 is Expr.Mul)) {
                        Expr.Add(bv, value1, value2)
                    } else {
                        Expr.Var(bv)
                    }
                }
                else -> Expr.Var(bv)
            }
        }

        override fun copyOperation(insn: AbstractInsnNode, value: Expr): Expr = Expr.Var(sourceInterpreter.copyOperation(insn, value.sv))

        override fun merge(value1: Expr, value2: Expr): Expr {
            if (value1 == value2) {
                return value1
            } else if (value1 is Expr.Mul && value2 is Expr.Mul && value1.insn == value2.insn) {
                if (value1.a == value2.a && value1.a is Expr.Const) {
                    return Expr.Mul(value1.sv, value1.a, merge(value1.b, value2.b)).also { mults[value1.insn] = it }
                } else if (value1.b == value2.b && value1.b is Expr.Const) {
                    return Expr.Mul(value1.sv, merge(value1.a, value2.a), value1.b).also { mults[value1.insn] = it }
                }
            } else if (value1 is Expr.Add && value2 is Expr.Add && value1.insn == value2.insn) {
                if (value1.a == value2.a && value1.a !is Expr.Var) {
                    val bb = merge(value1.b, value2.b)
                    if (bb is Expr.Const || bb is Expr.Mul) {
                        return Expr.Add(value1.sv, value1.a, bb)
                    }
                } else if (value1.b == value2.b && value2.b !is Expr.Var) {
                    val aa = merge(value1.a, value2.a)
                    if (aa is Expr.Const || aa is Expr.Mul) {
                        return Expr.Add(value1.sv, aa, value1.b)
                    }
                }
            }
            if (value1 is Expr.Mul) mults.remove(value1.insn)
            if (value2 is Expr.Mul) mults.remove(value2.insn)
            return Expr.Var(sourceInterpreter.merge(value1.sv, value2.sv))
        }

        override fun naryOperation(insn: AbstractInsnNode, values: MutableList<out Expr>): Expr? {
            return sourceInterpreter.naryOperation(insn, emptyList())?.let { Expr.Var(it) }
        }

        override fun newOperation(insn: AbstractInsnNode): Expr {
            val bv = sourceInterpreter.newOperation(insn)
            return when (insn.opcode) {
                Opcodes.LDC ->  {
                    val cst = (insn as LdcInsnNode).cst
                    when (cst) {
                        is Int, is Long -> Expr.Const(bv, cst as Number)
                        else -> Expr.Var(bv)
                    }
                }
                Opcodes.ICONST_1, Opcodes.LCONST_1 -> Expr.Const(bv, 1)
                Opcodes.ICONST_0, Opcodes.LCONST_0 -> Expr.Const(bv, 0)
                else -> Expr.Var(bv)
            }
        }

        override fun newValue(type: Type?): Expr? {
            return sourceInterpreter.newValue(type)?.let { Expr.Var(it) }
        }

        override fun returnOperation(insn: AbstractInsnNode, value: Expr, expected: Expr) {}

        override fun ternaryOperation(insn: AbstractInsnNode, value1: Expr, value2: Expr, value3: Expr): Expr? = null

        override fun unaryOperation(insn: AbstractInsnNode, value: Expr): Expr? {
            return sourceInterpreter.unaryOperation(insn, value.sv)?.let { Expr.Var(it) }
        }

        val constantMultiplications: Collection<Expr.Mul> get() {
            val ms = LinkedHashSet<Expr.Mul>()
            for (m in mults.values) {
                val other = m.other
                if (other is Expr.Mul) {
                    ms.remove(other)
                }
                if (other is Expr.Add && other.a is Expr.Mul) {
                    ms.remove(other.a)
                }
                if (other is Expr.Add && other.b is Expr.Mul) {
                    ms.remove(other.b)
                }
                ms.add(m)
            }
            return ms
        }
    }

    private sealed class Expr : Value {

        override fun getSize(): Int = sv.size

        abstract val sv: SourceValue

        val insn get() = sv.insns.single()

        data class Var(override val sv: SourceValue) : Expr() {

            override fun toString(): String = "(#${sv.hashCode().toString(16)})"
        }

        data class Const(override val sv: SourceValue, val n: Number) : Expr() {

            override fun toString(): String ="($n)"
        }

        data class Add(override val sv: SourceValue, val a: Expr, val b: Expr) : Expr() {

            override fun toString(): String {
                val c = if (insn.opcode == Opcodes.IADD || insn.opcode == Opcodes.LADD) '+' else '-'
                return "($a$c$b)"
            }
        }

        data class Mul(override val sv: SourceValue, val a: Expr, val b: Expr) : Expr() {

            val const get() = a as? Const ?: b as Const

            val other get() = if (const == a) b else a

            override fun toString(): String = "($a*$b)"
        }
    }

    fun InsnList.insertSafe(previousInsn: AbstractInsnNode, vararg insns: AbstractInsnNode) {
        check(contains(previousInsn))
        insns.reversed().forEach { insert(previousInsn, it) }
    }

    fun InsnList.insertBeforeSafe(nextInsn: AbstractInsnNode, vararg insns: AbstractInsnNode) {
        check(contains(nextInsn))
        insns.forEach { insertBefore(nextInsn, it) }
    }

    fun InsnList.removeSafe(vararg insns: AbstractInsnNode) {
        insns.forEach {
            check(contains(it))
            remove(it)
        }
    }

    fun InsnList.setSafe(oldInsn: AbstractInsnNode, newInsn: AbstractInsnNode) {
        check(contains(oldInsn))
        set(oldInsn, newInsn)
    }

    fun loadInt(n: Int): AbstractInsnNode = when (n) {
        in -1..5 -> InsnNode(n + 3)
        in Byte.MIN_VALUE..Byte.MAX_VALUE -> IntInsnNode(Opcodes.BIPUSH, n)
        in Short.MIN_VALUE..Short.MAX_VALUE -> IntInsnNode(Opcodes.SIPUSH, n)
        else -> LdcInsnNode(n)
    }

    fun loadLong(n: Long): AbstractInsnNode = when (n) {
        0L, 1L -> InsnNode((n + 9).toInt())
        else -> LdcInsnNode(n)
    }

    val AbstractInsnNode.isIntValue: Boolean get() {
        return when (opcode) {
            Opcodes.LDC -> (this as LdcInsnNode).cst is Int
            Opcodes.SIPUSH, Opcodes.BIPUSH, Opcodes.ICONST_0, Opcodes.ICONST_1, Opcodes.ICONST_2, Opcodes.ICONST_3, Opcodes.ICONST_4, Opcodes.ICONST_5, Opcodes.ICONST_M1 -> true
            else -> false
        }
    }

    val AbstractInsnNode.intValue: Int get() {
        if (opcode in 2..8) return opcode - 3
        if (opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH) return (this as IntInsnNode).operand
        if (this is LdcInsnNode && cst is Int) return cst as Int
        error(this)
    }
}
