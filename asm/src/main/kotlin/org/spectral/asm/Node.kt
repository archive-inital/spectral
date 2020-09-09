package org.spectral.asm

import org.objectweb.asm.Type

/**
 * A node is a type of object which has its roots as an element dealing with Bytecode of .class java files.
 *
 *
 * Some examples of nodes should be:
 * Classes, Methods, Fields, Parameters, Variables, etc.
 *
 * A class collection is NOT a node since it's only job is to hold a collection of class elements
 * and has nothing to do with bytecode.
 */
interface Node {

    /**
     * Name of the node typed element.
     */
    val name: String

    /**
     * The ASM [Type] of the node element.
     */
    val type: Type

    /**
     * The bit-packed access and visibility flags of the node element.
     */
    val access: Int
}