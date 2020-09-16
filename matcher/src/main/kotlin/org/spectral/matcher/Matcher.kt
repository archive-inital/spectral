package org.spectral.matcher

import org.spectral.asm.ClassPool

/**
 * Compares two [ClassPool] objects with different obfuscations and builds a
 * mapping graph between the two based on execution similarity.
 *
 * @property poolA ClassPool
 * @property poolB ClassPool
 * @constructor
 */
class Matcher(val poolA: ClassPool, val poolB: ClassPool) {


}