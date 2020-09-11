package org.spectral.asm

import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.util.jar.JarFile

class ClassPool {

    private val classMap = hashMapOf<String, Class>()

    val classes: List<Class> get() = classMap.values.toList()

    fun add(entry: Class) {
        if(!classes.contains(entry)) {
            classMap[entry.name] = entry
        }
    }

    fun remove(entry: Class) {
        classMap.remove(entry.name)
    }

    operator fun get(name: String): Class? = classMap[name]

    companion object {

        fun loadJar(file: File): ClassPool {
            val pool = ClassPool()

            JarFile(file).use { jar ->
                jar.entries().asSequence()
                    .filter { it.name.endsWith(".class") }
                    .forEach {
                        val node = ClassNode()
                        val reader = ClassReader(jar.getInputStream(it))

                        reader.accept(node, ClassReader.SKIP_FRAMES)

                        pool.add(Class(pool, node))
                    }
            }

            return pool
        }
    }
}