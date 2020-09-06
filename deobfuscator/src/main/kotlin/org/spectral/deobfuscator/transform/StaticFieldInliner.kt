package org.spectral.deobfuscator.transform

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes.ASM8
import org.objectweb.asm.Type
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.spectral.asm.core.Class
import org.spectral.asm.core.ClassPool
import org.spectral.asm.core.Field
import org.spectral.deobfuscator.Transformer
import org.tinylog.kotlin.Logger

/**
 * Moves static fields from random classes to known classes.
 *
 * If the static field has only one class reference, that it is inlined
 * as a private field into that class.
 *
 * If the field is static and has multiple method references that are not in the same class, The field is moved
 * into a common class named 'StaticFields'
 */
class StaticFieldInliner : Transformer {

    private var counter = 0

    private val inlinedFields = mutableListOf<InlinedField>()

    override fun execute(pool: ClassPool) {
        val fields = mutableListOf<Pair<Field, Class>>()

        pool.forEach classLoop@ { c ->
            c.fields.forEach fieldLoop@ { f ->
                if(!f.isStatic()) return@fieldLoop

                val classRefs = hashSetOf<Class>()

                f.writeRefs.forEach { classRefs.add(it.owner) }
                f.readRefs.forEach { classRefs.add(it.owner) }

                if(classRefs.size == 1 && classRefs.first() != f.owner) {
                    fields.add(f to classRefs.first())
                }
            }
        }

        fields.forEach {
            inlinedFields.add(inlineField(it.first, it.second))
        }

        pool.forEach { visitClass(it) }

        Logger.info("Inlined $counter static single reference fields.")
    }

    private fun inlineField(field: Field, toClass: Class): InlinedField {
        val pool = toClass.pool
        val toClassNode = toClass.node
        val fromClassNode = field.owner.node
        val oldFieldNode = field.node
        val newFieldNode = FieldNode(oldFieldNode.access, oldFieldNode.name, oldFieldNode.desc, oldFieldNode.signature, oldFieldNode.value)

        newFieldNode.name = "inlinedField${++counter}"

        fromClassNode.fields.remove(oldFieldNode)
        toClassNode.fields.add(newFieldNode)

        pool.elements[Type.getObjectType(fromClassNode.name)] = Class(pool, fromClassNode, Type.getObjectType(fromClassNode.name), true)
        pool.elements[Type.getObjectType(toClassNode.name)] = Class(pool, toClassNode, Type.getObjectType(toClassNode.name), true)

        return InlinedField(oldFieldNode, newFieldNode, fromClassNode, toClassNode)
    }

    private fun visitClass(cls: Class): ClassNode {
        val newNode = ClassNode()

        val visitor = object : ClassVisitor(ASM8, newNode) {
            override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor {
                return FieldInlineVisitor(
                        super.visitMethod(access, name, descriptor, signature, exceptions),
                        inlinedFields
                )
            }
        }

        cls.node.accept(visitor)

        return newNode
    }

    private class FieldInlineVisitor(
            visitor: MethodVisitor,
            private val inlinedFields: List<InlinedField>
    ) : MethodVisitor(ASM8, visitor) {
        override fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
            var found = false
            inlinedFields.forEach {
                if(owner == it.oldClass.name && name == it.oldField.name && descriptor == it.oldField.desc) {
                    super.visitFieldInsn(opcode, it.newClass.name, it.newField.name, it.newField.desc)
                    found = true
                }
            }

            if(!found) {
                super.visitFieldInsn(opcode, owner, name, descriptor)
            }
        }
    }

    private data class InlinedField(val oldField: FieldNode, val newField: FieldNode, val oldClass: ClassNode, val newClass: ClassNode)
}