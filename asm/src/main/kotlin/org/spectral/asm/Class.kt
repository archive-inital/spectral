package org.spectral.asm

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode

class Class(val pool: ClassPool, internal val node: ClassNode) : Node {

    override var name = node.name

    override var access = node.access

    var superName = node.superName

    override val type get() = Type.getObjectType(name)

    val interfaces = mutableListOf<String>()

    val parent get() = pool[superName]

    val interfaceClasses get() = interfaces.mapNotNull { pool[it] }

    val methods = node.methods.map { Method(this, it) }

    val fields = node.fields.map { Field(this, it) }

    fun accept(visitor: ClassVisitor) {

    }

    override fun toString(): String {
        return name
    }
}