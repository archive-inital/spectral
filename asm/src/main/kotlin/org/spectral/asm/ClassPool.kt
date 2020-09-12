package org.spectral.asm

import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.tree.ClassNode
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

class ClassPool {

    private val classMap = hashMapOf<String, Class>()

    val classes: List<Class> get() {
        return classMap.values.toList()
    }

    val size: Int get() = classes.size

    private var processed = false

    fun process() {
        if(processed) throw IllegalStateException("Pool has already been processed.")
        processed = true

        classes.forEach { it.process() }
    }

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

    fun saveJar(file: File) {
        if(file.exists()) {
            file.delete()
        }

        val jos = JarOutputStream(FileOutputStream(file))

        this.classes.forEach { cls ->
            jos.putNextEntry(JarEntry(cls.name + ".class"))

            val writer = ClassWriter(ClassWriter.COMPUTE_MAXS)
            cls.accept(writer)

            jos.write(writer.toByteArray())
            jos.closeEntry()
        }

        jos.close()
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

            pool.process()

            return pool
        }
    }
}