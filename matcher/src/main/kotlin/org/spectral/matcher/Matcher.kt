package org.spectral.matcher

import org.spectral.asm.ClassPool
import org.tinylog.kotlin.Logger
import java.io.File

/**
 * Compares two [ClassPool] objects with different obfuscations and builds a
 * mapping graph between the two based on execution similarity.
 *
 * @property poolA ClassPool
 * @property poolB ClassPool
 * @constructor
 */
class Matcher(val poolA: ClassPool, val poolB: ClassPool) {

    /**
     * The global match group.
     */
    val matchGroup = MatchGroup(poolA, poolB)

    /**
     * Creates a [Matcher] instance from two JAR files.
     *
     * @param jarFileA The mapped JAR file
     * @param jarFileB The unmapped JAR file.
     * @constructor
     */
    constructor(jarFileA: File, jarFileB: File) : this(ClassPool.loadJar(jarFileA), ClassPool.loadJar(jarFileB))

    /**
     * Runs the matcher.
     */
    fun run() {
        Logger.info("Preparing to match classes...")


    }
}