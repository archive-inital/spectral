package org.spectral.asm

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.tree.MethodNode

class Method(val owner: Class, val node: MethodNode) : Node {

    val pool = owner.pool

    override var name = node.name

    val desc get() = Type.getMethodType(returnType, *argumentTypes.toTypedArray()).toString()

    override var access = node.access

    override val type get() = Type.getMethodType(node.desc)

    val argumentTypes = type.argumentTypes.toMutableList()

    var returnType = type.returnType

    var instructions = node.instructions

    val exceptions = node.exceptions.toMutableList()

    var tryCatchBlocks = node.tryCatchBlocks.toMutableList()

    var maxStack = node.maxStack

    var maxLocals = node.maxLocals

    fun process() {

    }

    fun accept(visitor: MethodVisitor) {
        node.parameters?.forEach {
            visitor.visitParameter(it.name, it.access)
        }

        node.visibleAnnotations?.forEach {
            it.accept(visitor.visitAnnotation(it.desc, true))
        }

        visitor.visitCode()
        tryCatchBlocks.forEach {
            visitor.visitTryCatchBlock(
                    it.start.label,
                    it.end.label,
                    it.handler.label,
                    it.type
            )
        }

        instructions.forEach {
            it.accept(visitor)
        }

        visitor.visitMaxs(maxStack, maxLocals)

        visitor.visitEnd()
    }

    override fun toString(): String {
        return "$owner.$name$desc"
    }
}