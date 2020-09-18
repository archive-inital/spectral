package org.spectral.matcher

import proguard.classfile.ClassPool
import proguard.classfile.visitor.ClassNameFilter
import proguard.classfile.visitor.ClassPoolFiller
import proguard.io.*
import java.io.File
import java.io.IOException


/**
 * This utility class provides methods to read and write the classes in jars.
 */
object JarUtil {
    /**
     * Reads the classes from the specified jar file and returns them as a class
     * pool.
     *
     * @param jarFileName the name of the jar file or jmod file.
     * @param isLibrary   specifies whether classes should be represented as
     * ProgramClass instances (for processing) or
     * LibraryClass instances (more compact).
     * @return a new class pool with the read classes.
     */
    @Throws(IOException::class)
    fun readJar(
        jarFileName: String?,
        isLibrary: Boolean
    ): ClassPool {
        return readJar(jarFileName, "**", isLibrary)
    }

    /**
     * Reads the classes from the specified jar file and returns them as a class
     * pool.
     *
     * @param jarFileName the name of the jar file or jmod file.
     * @param isLibrary   specifies whether classes should be represented as
     * ProgramClass instances (for processing) or
     * LibraryClass instances (more compact).
     * @return a new class pool with the read classes.
     */
    @Throws(IOException::class)
    fun readJar(
        jarFileName: String?,
        classNameFilter: String?,
        isLibrary: Boolean
    ): ClassPool {
        val classPool = ClassPool()

        // Parse all classes from the input jar and
        // collect them in the class pool.
        val source: DataEntrySource = FileSource(
            File(jarFileName)
        )
        source.pumpDataEntries(
            JarReader(
                isLibrary,
                ClassFilter(
                    ClassReader(
                        isLibrary, false, false, false, null,
                        ClassNameFilter(
                            classNameFilter,
                            ClassPoolFiller(classPool)
                        )
                    )
                )
            )
        )
        return classPool
    }

    /**
     * Writes the classes from the given class pool to a specified jar.
     * @param programClassPool  the classes to write.
     * @param outputJarFileName the name of the output jar file.
     */
    @Throws(IOException::class)
    fun writeJar(
        programClassPool: ClassPool,
        outputJarFileName: String?
    ) {
        val jarWriter = JarWriter(
            ZipWriter(
                FixedFileWriter(
                    File(outputJarFileName)
                )
            )
        )
        programClassPool.classesAccept(
            DataEntryClassWriter(jarWriter)
        )
        jarWriter.close()
    }
}