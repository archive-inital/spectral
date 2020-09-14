package org.spectral.deobfuscator.transformer.opaque

import com.google.common.collect.MultimapBuilder
import com.google.common.collect.TreeMultimap
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*
import org.spectral.asm.Class
import org.spectral.asm.ClassPool
import org.spectral.asm.Method
import org.spectral.deobfuscator.Transformer
import org.spectral.deobfuscator.common.Transform
import org.tinylog.kotlin.Logger
import java.lang.reflect.Modifier
import java.util.*

/**
 * Removes the garbage method arguments primitives.
 */
@Transform(priority = 9)
class OpaquePredicateArgumentRemover : Transformer {

    override fun transform(pool: ClassPool) {
        val namedGroup = pool.classes.map { it }.associateBy { it.name }

        val changedMethods = TreeMap<String, String>()
        var changedInsnCounter = 0

        val topMethods = hashSetOf<String>()
        for(c in pool.classes) {
            val supers = supers(c, namedGroup)
            for(m in c.methods) {
                if(supers.none { it.methods.any { it.name == m.name && it.desc == m.desc } }) {
                    topMethods.add("${c.name}.${m.name}${m.desc}")
                }
            }
        }

        val implementationMap = MultimapBuilder.hashKeys().arrayListValues().build<String, ClassMethod>()
        val implementationFlatMap = implementationMap.asMap()

        for(c in pool.classes) {
            for(m in c.methods) {
                val s = overrides(c.name, m.name + m.desc, topMethods, namedGroup) ?: continue
                implementationMap.put(s, ClassMethod(c, m))
            }
        }

        val it = implementationFlatMap.iterator()
        for(e in it) {
            if(e.value.any { !hasUnusedLastParamInt(it.m) }) {
                it.remove()
            }
        }

        for(c in pool.classes) {
            for(m in c.methods) {
                val insnList = m.instructions
                for(insn in insnList) {
                    if(insn !is MethodInsnNode) continue
                    val s = overrides(insn.owner, insn.name + insn.desc, implementationFlatMap.keys, namedGroup) ?: continue
                    if(!insn.previous.isIntValue()) {
                        implementationFlatMap.remove(s)
                    }
                }
            }
        }

        val duplicateMap = TreeMultimap.create<String, String>()
        pool.classes.forEach { c ->
            c.methods.filter { it.name != "<clinit>" }.forEach { m ->
                duplicateMap.put(m.id(), c.name + "." + m.name + m.desc)
            }
        }

        implementationMap.values().forEach { (c, m) ->
            val oldDesc = m.desc
            val newDesc = dropLastArg(oldDesc)
            val newType = Type.getMethodType(newDesc)
            val newReturnType = newType.returnType
            val newArgTypes = newType.argumentTypes

            m.returnType = newReturnType
            m.argumentTypes.clear()
            m.argumentTypes.addAll(newArgTypes)

            changedMethods["${c.name}.${m.name}$newDesc"] = oldDesc
        }

        for(c in pool.classes) {
            for(m in c.methods) {
                val insnList = m.instructions
                for(insn in insnList) {
                    if(insn !is MethodInsnNode) continue
                    if(overrides(insn.owner, insn.name + insn.desc, implementationFlatMap.keys, namedGroup) != null) {
                        insn.desc = dropLastArg(insn.desc)
                        val prev = insn.previous
                        check(prev.isIntValue())
                        insnList.remove(prev)
                        changedInsnCounter++
                    }
                }
            }
        }

        Logger.info("Removed opaque predicate arguments: [methods: ${changedMethods.size}, instructions: $changedInsnCounter].")
    }

    private fun overrides(owner: String, nameDesc: String, methods: Set<String>, classNames: Map<String, Class>): String? {
        val s = "$owner.$nameDesc"
        if (s in methods) return s
        if (nameDesc.startsWith("<init>")) return null
        val classNode = classNames[owner] ?: return null
        for (sup in supers(classNode, classNames)) {
            return overrides(sup.name, nameDesc, methods, classNames) ?: continue
        }
        return null
    }

    private fun supers(c: Class, classNames: Map<String, Class>): Collection<Class> {
        return c.interfaces.plus(c.superName).mapNotNull { classNames[it] }.flatMap { supers(it, classNames).plus(it) }
    }

    private fun hasUnusedLastParamInt(m: Method): Boolean {
        val argTypes = Type.getArgumentTypes(m.desc)
        if (argTypes.isEmpty()) return false
        val lastArg = argTypes.last()
        if (lastArg != Type.BYTE_TYPE && lastArg != Type.SHORT_TYPE && lastArg != Type.INT_TYPE) return false
        if (Modifier.isAbstract(m.access)) return true
        val lastParamLocalIndex = (if (Modifier.isStatic(m.access)) -1 else 0) + (Type.getArgumentsAndReturnSizes(m.desc) shr 2) - 1
        for (insn in m.instructions) {
            if (insn !is VarInsnNode) continue
            if (insn.`var` == lastParamLocalIndex) return false
        }
        return true
    }

    private fun dropLastArg(desc: String): String {
        val type = Type.getMethodType(desc)
        return Type.getMethodDescriptor(type.returnType, *type.argumentTypes.copyOf(type.argumentTypes.size - 1))
    }

    private data class ClassMethod(val c: Class, val m: Method)

    private fun Method.id(): String {
        return "${ Type.getReturnType(desc)}." + (instructions.lineNumberRange() ?: "*") + "." + instructions.hash()
    }

    private fun InsnList.lineNumberRange(): IntRange? {
        val lns = iterator().asSequence().mapNotNull { it as? LineNumberNode }.mapNotNull { it.line }.toList()
        if(lns.isEmpty()) return null
        return lns.first()..lns.last()
    }

    private fun InsnList.hash(): Int {
        return iterator().asSequence().mapNotNull {
            when(it) {
                is FieldInsnNode -> it.owner + "." + it.name + ":" + it.opcode
                is MethodInsnNode -> it.opcode.toString() + ":" + it.owner + "." + it.name
                is InsnNode -> it.opcode.toString()
                else -> null
            }
        }.toSet().hashCode()
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
}