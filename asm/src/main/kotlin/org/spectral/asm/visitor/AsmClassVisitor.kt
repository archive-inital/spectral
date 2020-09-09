package org.spectral.asm.visitor

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes.ASM8
import org.spectral.asm.Class

/**
 * An implementation of the ASM [ClassVisitor] which creates [Class] instances
 * by the visitor pattern.
 */
class AsmClassVisitor : ClassVisitor(ASM8) {

    /**
     * The [Class] object which will be visited.
     */
    private val cls = Class()

    /**
     * Gets the resulting [Class] object after visiting.
     *
     * @return The visited [Class] result.
     */
    fun toClass(): Class = cls

    /**
     * Visits the [Class] object of [cls] and updates all the corrisponding
     * method arguments inside of the [cls] instance.
     *
     * This is the standard visitor patter.
     *
     * @param version Int
     * @param access Int
     * @param name String
     * @param signature String
     * @param superName String
     * @param interfaces Array<out String>
     */
    override fun visit(
        version: Int,
        access: Int,
        name: String,
        signature: String?,
        superName: String,
        interfaces: Array<out String>
    ) {
        cls.name = name
        cls.superName = superName
        cls.version = version
        cls.access = access

        interfaces.forEach { itf ->
            cls.interfaces.addInterface(itf)
        }
    }

    /**
     * Visits the [Class] source and sets the field to the filename of where the visitor came from.
     *
     * @param source Origin file name of the visitor
     * @param debug Unused debug name
     */
    override fun visitSource(source: String, debug: String?) {
        cls.source = source
    }
}