package org.spectral.asm.code

import org.spectral.asm.Method

/**
 * Represents the instruction list or code of a provided [Method] object.
 *
 * @property method The [Method] this code is for.
 * @constructor Creates a new [Code] instance for a given [method]
 */
class Code(val method: Method) {

    val instructions = Instructions(this)

}