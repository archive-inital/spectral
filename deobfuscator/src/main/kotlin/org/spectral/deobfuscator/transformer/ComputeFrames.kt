package org.spectral.deobfuscator.transformer

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.spectral.asm.ClassPool
import org.spectral.deobfuscator.Transformer
import org.spectral.asm.Class
import org.spectral.deobfuscator.common.Transform

@Transform(priority = 13)
class ComputeFrames : Transformer {

    override fun transform(pool: ClassPool) {
        val rebuiltClasses = mutableListOf<Class>()

        pool.classes.forEach { c ->
            val newNode = ClassNode()
            val writer = Writer(pool.classes.associate { it.name to it.node })
            c.accept(writer)

            val reader = ClassReader(writer.toByteArray())
            reader.accept(newNode, ClassReader.SKIP_FRAMES)

            rebuiltClasses.add(Class(pool, newNode))
        }

        pool.clear()

        rebuiltClasses.forEach { c ->
            pool.add(c)
        }
    }

    private class Writer(private val classNames: Map<String, ClassNode>) : ClassWriter(COMPUTE_FRAMES or COMPUTE_MAXS) {

        companion object {
            val OBJECT_INTERNAL_NAME: String = Type.getInternalName(Any::class.java)
        }

        override fun getCommonSuperClass(type1: String, type2: String): String {
            if (isAssignable(type1, type2)) return type1
            if (isAssignable(type2, type1)) return type2
            var t1 = type1
            do {
                t1 = checkNotNull(superClassName(t1, classNames))
            } while (!isAssignable(t1, type2))
            return t1
        }

        private fun isAssignable(to: String, from: String): Boolean {
            if (to == from) return true
            val sup = superClassName(from, classNames) ?: return false
            if (isAssignable(to, sup)) return true
            return interfaceNames(from).any { isAssignable(to, it) }
        }

        private fun interfaceNames(type: String): List<String> {
            return if (type in classNames) {
                classNames.getValue(type).interfaces
            } else {
                java.lang.Class.forName(type.replace('/', '.')).interfaces.map { Type.getInternalName(it) }
            }
        }

        private fun superClassName(type: String, classNames: Map<String, ClassNode>): String? {
            return if (type in classNames) {
                classNames.getValue(type).superName
            } else {
                val c = java.lang.Class.forName(type.replace('/', '.'))
                if (c.isInterface) {
                    OBJECT_INTERNAL_NAME
                } else {
                    c.superclass?.let { Type.getInternalName(it) }
                }
            }
        }
    }
}