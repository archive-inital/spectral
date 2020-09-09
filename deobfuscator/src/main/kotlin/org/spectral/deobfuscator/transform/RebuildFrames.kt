package org.spectral.deobfuscator.transform

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.spectral.asm.core.ClassPool
import org.spectral.deobfuscator.Transformer
import org.tinylog.kotlin.Logger

/**
 * Rebuilds the frames in each method inside the pool by writing and reading the class
 * pool using the COMPUTE_FRAMES flag.
 */
class RebuildFrames : Transformer {

    override fun execute(pool: ClassPool) {
        val newPool = ClassPool.create()
        pool.values.forEach { c ->
            val writer = Writer(pool.values.associate { it.name to it.node })
            c.node.accept(writer)

            val newNode = ClassNode()
            val reader = ClassReader(writer.toByteArray())
            reader.accept(newNode, ClassReader.SKIP_FRAMES)

            newPool.addClass(Class(newPool, Type.getObjectType(newNode.name), newNode))
        }

        pool.clear()
        pool.putAll(newPool)

        pool.values.forEach { it.node.accept(it) }

        Logger.info("Rebuilt class pool instruction frames.")
    }

    private class Writer(private val classNames: Map<String, ClassNode>) : ClassWriter(COMPUTE_FRAMES) {

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
                Class.forName(type.replace('/', '.')).interfaces.map { Type.getInternalName(it) }
            }
        }

        private fun superClassName(type: String, classNames: Map<String, ClassNode>): String? {
            return if (type in classNames) {
                classNames.getValue(type).superName
            } else {
                val c = Class.forName(type.replace('/', '.'))
                if (c.isInterface) {
                    OBJECT_INTERNAL_NAME
                } else {
                    c.superclass?.let { Type.getInternalName(it) }
                }
            }
        }
    }
}