package org.spectral.matcher.util

import org.objectweb.asm.Type

/**
 * Whether a given ASM [Type] is a primitive data type.
 */
val Type.isPrimitive: Boolean get() {
    return when(this.sort) {
        Type.BOOLEAN, Type.BYTE, Type.SHORT, Type.CHAR, Type.INT,
        Type.VOID, Type.LONG, Type.FLOAT, Type.DOUBLE -> true
        else -> false
    }
}