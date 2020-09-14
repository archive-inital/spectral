package org.spectral.asm

import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.tree.FieldNode

class Field(val owner: Class, val node: FieldNode) : Node {

    val pool = owner.pool

    override var name = node.name

    val desc get() = type.toString()

    override var type = Type.getType(node.desc)

    override var access = node.access

    var value = node.value

    fun process() {

    }

    fun accept(visitor: FieldVisitor) {
        node.visibleAnnotations?.forEach {
            it.accept(visitor.visitAnnotation(it.desc, true))
        }

        visitor.visitEnd()
    }

    override fun toString(): String {
        return "$owner.$name"
    }
}