package org.spectral.asm

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode

class Class(val pool: ClassPool, internal val node: ClassNode) : Node {

    var version = node.version

    var source = node.sourceFile

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
        visitor.visit(version, access, name, null, superName, interfaces.toTypedArray())
        visitor.visitSource(source, null)

        node.visibleAnnotations.forEach {
            it.accept(visitor.visitAnnotation(it.desc, true))
        }

        fields.forEach { f ->
            val fv = visitor.visitField(f.access, f.name, f.desc, null, f.value)
            f.accept(fv)
        }

        methods.forEach { m ->
            val exceptions: Array<String> = m.exceptions.toTypedArray()
            val mv = visitor.visitMethod(m.access, m.name, m.desc, null, if(exceptions.isEmpty()) null else exceptions)
            m.accept(mv)
        }

        visitor.visitEnd()
    }

    override fun toString(): String {
        return name
    }
}