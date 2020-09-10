package org.spectral.asm

import org.objectweb.asm.ClassReader
import org.spectral.asm.visitor.AsmClassVisitor
import java.io.File
import java.util.jar.JarFile

/**
 * Contains utility methods for loading classes from JAR Files.
 */
object JarUtil {

    /**
     * Loads classes from a jar file and puts all entries into
     * a single [ClassPool] object.
     *
     * @param file File
     * @return ClassPool
     */
    fun loadJar(file: File): ClassPool {
        val pool = ClassPool()

        JarFile(file).use { jar ->
            jar.entries().asSequence()
                .filter { it.name.endsWith(".class") }
                .forEach {
                    val cv = AsmClassVisitor()
                    val reader = ClassReader(jar.getInputStream(it))

                    reader.accept(cv, ClassReader.SKIP_FRAMES)

                    pool.addClass(cv.toClass())
                }
        }

        return pool
    }
}