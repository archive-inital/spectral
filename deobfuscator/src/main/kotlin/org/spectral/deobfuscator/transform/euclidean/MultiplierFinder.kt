package org.spectral.deobfuscator.transform.euclidean

import com.google.common.collect.MultimapBuilder
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AbstractInsnNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.analysis.*
import org.spectral.asm.core.ClassPool
import org.tinylog.kotlin.Logger
import kotlin.math.absoluteValue

/**
 * Responsible for locating and solving for the inverse
 * modulus multipliers.
 */
class MultiplierFinder {

    /**
     * Gets the multipliers for the primitive field getters for a
     *
     * @param pool ClassPool
     * @return Map<String, Long>
     */
    fun getMultipliers(pool: ClassPool): Map<String, Long> {
        Logger.info("Calculating multipliers.")

        val multipliers = Multipliers()
        val analyzer = Analyzer(Interpret(multipliers))
        pool.values.forEach { c ->
            c.methods.forEach { m ->
                analyzer.analyze(c.name, m.node)
            }
        }

        multipliers.solve()

        return multipliers.decoders.mapValues { it.value.toLong() }
    }

    private class Interpret(private val multipliers: Multipliers) : Interpreter<Valu>(Opcodes.ASM8) {

        private fun isMultiplier(n: Number) = isInvertible(n) && invert(n) != n

        private val ldcs = HashSet<Valu>()

        private val ldcs2 = HashSet<Valu>()

        private val puts = HashMap<Valu, Valu>()

        private val src = SourceInterpreter()

        override fun newValue(type: Type?) = src.newValue(type)?.let { Valu(it) }

        override fun copyOperation(insn: AbstractInsnNode, value: Valu) = when (insn.opcode) {
            Opcodes.DUP, Opcodes.DUP2, Opcodes.DUP2_X1, Opcodes.DUP_X1 -> value
            else -> Valu(src.copyOperation(insn, value.v))
        }

        override fun merge(value1: Valu, value2: Valu) = Valu(src.merge(value1.v, value2.v))

        override fun returnOperation(insn: AbstractInsnNode, value: Valu, expected: Valu) {}

        override fun ternaryOperation(insn: AbstractInsnNode, value1: Valu, value2: Valu, value3: Valu) = Valu(src.ternaryOperation(insn, value1.v, value2.v, value3.v))

        override fun naryOperation(insn: AbstractInsnNode, values: MutableList<out Valu>) = Valu(src.naryOperation(insn, values.map { it.v }))

        override fun newOperation(insn: AbstractInsnNode) = Valu(src.newOperation(insn))

        override fun unaryOperation(insn: AbstractInsnNode, value: Valu) = Valu(src.unaryOperation(insn, value.v)).also {
            if (insn.opcode == Opcodes.PUTSTATIC) setField(it, value)
        }

        override fun binaryOperation(insn: AbstractInsnNode, value1: Valu, value2: Valu) = Valu.Two(src.binaryOperation(insn, value1.v, value2.v), value1, value2).also {
            when (insn.opcode) {
                Opcodes.IMUL, Opcodes.LMUL -> {
                    val fieldMul = asFieldMul(it) ?: return@also
                    if (ldcs.add(fieldMul.ldc)) {
                        multipliers.mulX.put(fieldMul.f.fieldName, Mul.dec(fieldMul.ldc.ldcNum))
                    }
                }
                Opcodes.PUTFIELD -> setField(it, value2)
            }
        }

        private fun setField(put: Valu, value: Valu) {
            puts[value] = put
            if (value.isLdcInt) {
                //
            } else if (value is Valu.Two) {
                distribute(put.v.insn as FieldInsnNode, value)
            }
        }

        private fun distribute(put: FieldInsnNode, value: Valu.Two) {
            if (value.isMul) {
                val fm = asFieldMul(value)
                if (fm != null && ldcs2.add(fm.ldc)) {
                    check(multipliers.mulX.remove(fm.f.fieldName, Mul.dec(fm.ldc.ldcNum)))
                    multipliers.decEncX.add(FieldMulAssign(put.fieldName, fm.f.fieldName, fm.ldc.ldcNum))
                    return
                }
            }
            if (!value.isMul && !value.isAdd) return
            val a = value.a
            val b = value.b
            var ldc: Valu? = null
            var other: Valu? = null
            if (a.isLdcInt) {
                ldc = a
                other = b
            } else if (b.isLdcInt) {
                ldc = b
                other = a
            }
            if (ldc != null && other != null) {
                val n = ldc.ldcNum
                if (isMultiplier(n) && ldcs.add(ldc)) {
                    val getField = puts[other]
                    if (getField == null) {
                        multipliers.mulX.put(put.fieldName, Mul.enc(n))
                    } else {
                        multipliers.decEncX.add(FieldMulAssign(put.fieldName, getField.fieldName, n))
                    }
                }
                if (value.isMul) return
            }
            if (a is Valu.Two) distribute(put, a)
            if (b is Valu.Two) distribute(put, b)
        }

        private fun asFieldMul(value: Valu.Two): FieldMul? {
            var ldc: Valu? = null
            var get: Valu? = null
            if (value.a.isLdcInt && value.b.isGetField) {
                ldc = value.a
                get = value.b
            } else if (value.b.isLdcInt && value.a.isGetField) {
                ldc = value.b
                get = value.a
            }
            if (ldc != null && get != null) {
                if (isMultiplier(ldc.ldcNum)) return FieldMul(get, ldc)
            }
            return null
        }

        private val Valu.isLdcInt get() = v.insn.let { it != null && it is LdcInsnNode && (it.cst is Int || it.cst is Long) }

        private val SourceValue.insn: AbstractInsnNode? get() = insns.singleOrNull()

        private val Valu.isGetField get() = v.insn.let { it != null && (it.opcode == Opcodes.GETSTATIC || it.opcode == Opcodes.GETFIELD) }

        private val Valu.ldcNum get() = v.insns.single().let { it as LdcInsnNode; it.cst as Number }

        private val FieldInsnNode.fieldName get() = "${owner}.${name}"

        private val Valu.fieldName get() = v.insns.single().let { it as FieldInsnNode; it.fieldName }

        private val Valu.isMul get() = v.insn.let { it != null && (it.opcode == Opcodes.IMUL || it.opcode == Opcodes.LMUL) }

        private val Valu.isAdd get() = v.insn.let { it != null && (it.opcode == Opcodes.IADD || it.opcode == Opcodes.LADD || it.opcode == Opcodes.ISUB || it.opcode == Opcodes.LSUB) }

        private data class FieldMul(val f: Valu, val ldc: Valu)
    }

    private open class Valu(val v: SourceValue) : Value {

        override fun equals(other: Any?) = other is Valu && v == other.v

        override fun hashCode() = v.hashCode()

        override fun getSize() = v.size

        class Two(value: SourceValue, val a: Valu, val b: Valu) : Valu(value)
    }

    private data class Mul(val dec: Boolean, val n: Number) {

        val decoder = if (dec) n else invert(n)

        companion object {

            fun dec(n: Number) = Mul(true, n)

            fun enc(n: Number) = Mul(false, n)
        }
    }

    private data class FieldMulAssign(val put: String, val get: String, val mul: Number)

    private class Multipliers {

        val decoders = HashMap<String, Number>()

        val mulX = MultimapBuilder.hashKeys().arrayListValues().build<String, Mul>()

        val decEncX = HashSet<FieldMulAssign>()

        private fun isMultiplier(n: Number) = isInvertible(n) && invert(n) != n

        fun solve() {
            while (true) {
                simplify()
                if (mulX.isEmpty) return
                solveOne()
            }
        }

        private fun simplify() {
            val itr = decEncX.iterator()
            for (ma in itr) {
                if (ma.put in decoders) {
                    itr.remove()
                    val dec = decoders.getValue(ma.put)
                    val decx = mul(dec, ma.mul)
                    if (isMultiplier(decx)) mulX.put(ma.get, Mul.dec(decx))
                } else if (ma.get in decoders) {
                    itr.remove()
                    val enc = invert(decoders.getValue(ma.get))
                    val encx = mul(enc, ma.mul)
                    if (isMultiplier(encx)) mulX.put(ma.put, Mul.enc(encx))
                }
            }
        }

        private fun solveOne() {
            var e = mulX.asMap().entries.firstOrNull { e -> decEncX.none { it.get == e.key || it.put == e.key } }
            if (e == null) e = mulX.asMap().entries.first()
            val (f, ms) = e
            decoders[f] = unfold(ms)
            mulX.removeAll(f)
        }

        private fun unfold(ms: Collection<Mul>): Number {
            val distinct = ms.distinct()
            if (distinct.size == 1) return distinct.single().decoder
            val pairs = distinct.filter { a -> a.dec && distinct.any { b -> !b.dec && a.decoder == b.decoder } }
            if (pairs.isNotEmpty()) return pairs.single().decoder
            val fs = distinct.filter { f -> distinct.all { isFactor(it, f) } }
            if (fs.size == 1) return fs.single().decoder
            check(fs.size == 2 && fs[0].dec != fs[1].dec)

            /**
             * Removed in rev 191
             */
            /*val counts = HashMultiset.create(ms)
            val maxCount = counts.entrySet().maxBy { it.count }!!.count
            val maxs = counts.entrySet().filter { it.count == maxCount }
            if (maxs.size == 1) return maxs.single().element.decoder*/

            return fs.first { it.dec }.decoder
        }

        private fun isFactor(product: Mul, factor: Mul) = div(product, factor).toLong().absoluteValue <= 0xff

        private fun div(a: Mul, b: Mul): Number {
            return if (a.dec == b.dec) {
                mul(invert(b.n), a.n)
            } else {
                mul(b.n, a.n)
            }
        }

        private fun mul(a: Number, b: Number): Number = when (a) {
            is Int -> a.toInt() * b.toInt()
            is Long -> a.toLong() * b.toLong()
            else -> error(a)
        }
    }
}