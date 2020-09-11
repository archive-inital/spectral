package org.spectral.asm

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.tree.MethodNode

class Method(val owner: Class, internal val node: MethodNode) : Node {

    val pool = owner.pool

    override var name = node.name

    val desc get() = Type.getMethodType(returnType, *argumentTypes.toTypedArray())

    override var access = node.access

    override val type get() = Type.getMethodType(node.desc)

    val argumentTypes = type.argumentTypes.toMutableList()

    var returnType = type.returnType

    val instructions = node.instructions

    val exceptions = node.exceptions.toMutableList()

    val tryCatchBlocks = node.tryCatchBlocks.toMutableList()

    fun accept(visitor: MethodVisitor) {

    }

    override fun toString(): String {
        return "$owner.$name$desc"
    }
}