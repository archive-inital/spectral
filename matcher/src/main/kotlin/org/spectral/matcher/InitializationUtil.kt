package org.spectral.matcher

import proguard.classfile.ClassPool
import proguard.classfile.util.ClassReferenceInitializer

import proguard.classfile.util.ClassSuperHierarchyInitializer

import proguard.classfile.util.WarningPrinter

import java.io.PrintWriter


/**
 * This utility class provides a method to initialize the cached
 * cross-references classes. They are necessary to traverse the class
 * hierarchy efficiently, for example when preverifying code or
 * performing more general partial evaluation.
 */
object InitializationUtil {
    /**
     * Initializes the cached cross-references of the classes in the given
     * class pools.
     * @param programClassPool the program class pool, typically with processed
     * classes.
     * @param libraryClassPool the library class pool, typically with run-time
     * classes.
     */
    fun initialize(
        programClassPool: ClassPool,
        libraryClassPool: ClassPool
    ) {
        // We may get some warnings about missing dependencies.
        // They're a pain, but for proper results, we really need to have
        // all dependencies.
        val printWriter = PrintWriter(System.err)
        val warningPrinter = WarningPrinter(printWriter)

        // Initialize the class hierarchies.
        libraryClassPool.classesAccept(
            ClassSuperHierarchyInitializer(
                programClassPool,
                libraryClassPool,
                null,
                null
            )
        )
        programClassPool.classesAccept(
            ClassSuperHierarchyInitializer(
                programClassPool,
                libraryClassPool,
                warningPrinter,
                warningPrinter
            )
        )

        // Initialize the other references from the program classes.
        programClassPool.classesAccept(
            ClassReferenceInitializer(
                programClassPool,
                libraryClassPool,
                warningPrinter,
                warningPrinter,
                warningPrinter,
                null
            )
        )

        // Flush the warnings.
        printWriter.flush()
    }
}