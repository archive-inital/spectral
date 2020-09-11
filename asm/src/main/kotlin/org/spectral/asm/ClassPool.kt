package org.spectral.asm

import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.util.jar.JarFile

class ClassPool {

    private val classMap = hashMapOf<String, Class>()

    val classes: List<Class> get() = classMap.values.toList()

    val size: Int get() = classes.size

    fun add(classFile: File) {
        add(classFile.inputStream().readAllBytes())
    }

    fun add(bytes: ByteArray) {
        val node = ClassNode()
        val reader = ClassReader(bytes)

        reader.accept(node, ClassReader.SKIP_FRAMES)

        add(node)
    }

    fun add(node: ClassNode) {
        add(Class(this, node))
    }

    fun add(entry: Class) {
        if(!classes.contains(entry)) {
            classMap[entry.name] = entry
        }
    }

    fun remove(entry: Class) {
        classMap.remove(entry.name)
    }

    operator fun get(name: String): Class? = classMap[name]

    operator fun set(name: String, cls: Class) {
        classMap[name] = cls
    }

    companion object {

        fun loadJar(file: File): ClassPool {
            val pool = ClassPool()

            JarFile(file).use { jar ->
                jar.entries().asSequence()
                    .filter { it.name.endsWith(".class") }
                    .forEach {
                        pool.add(jar.getInputStream(it).readAllBytes())
                    }
            }

            return pool
        }
    }
}