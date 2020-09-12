package org.spectral.asm

import org.objectweb.asm.Type

interface Node {

    val name: String

    val access: Int

    val type: Type

}