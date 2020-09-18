package org.spectral.matcher

import proguard.classfile.ClassPool
import proguard.classfile.Clazz
import proguard.classfile.Method
import proguard.classfile.attribute.Attribute
import proguard.classfile.attribute.CodeAttribute
import proguard.classfile.attribute.visitor.AllAttributeVisitor
import proguard.classfile.attribute.visitor.AttributeVisitor
import proguard.classfile.instruction.Instruction
import proguard.classfile.instruction.visitor.InstructionVisitor
import proguard.classfile.visitor.AllMemberVisitor
import proguard.classfile.visitor.ClassNameFilter
import proguard.classfile.visitor.MemberNameFilter
import proguard.evaluation.BasicInvocationUnit
import proguard.evaluation.PartialEvaluator
import proguard.evaluation.ReferenceTracingInvocationUnit
import proguard.evaluation.ReferenceTracingValueFactory
import proguard.evaluation.value.BasicValueFactory
import proguard.evaluation.value.Value

object Test {

    val poolA = JarUtil.readJar("gamepack.jar", false)
    val poolB = JarUtil.readJar("gamepack-deob.jar", false)

    val valueFactory = ReferenceTracingValueFactory(BasicValueFactory())
    val invocationUnit = ReferenceTracingInvocationUnit(BasicInvocationUnit(valueFactory))
    val evaluator = PartialEvaluator(valueFactory, invocationUnit, false, valueFactory as InstructionVisitor)

    @JvmStatic
    fun main(args: Array<String>) {
        println("Executing poolA")

        val resultA = ExecutionProcessor(evaluator)
        val resultB = ExecutionProcessor(evaluator)

        poolA.classesAccept(ClassNameFilter("**", AllMemberVisitor(MemberNameFilter("init", AllAttributeVisitor(resultA)))))

        println("Executing PoolB")
        poolB.classesAccept(ClassNameFilter("**", AllMemberVisitor(MemberNameFilter("init", AllAttributeVisitor(resultB)))))

        println()
    }

    private class ExecutionProcessor(val evaluator: PartialEvaluator) : AttributeVisitor, InstructionVisitor {
        val contexts = hashMapOf<Int, InstructionContext>()
        override fun visitAnyAttribute(clazz: Clazz?, attribute: Attribute?) {}
        override fun visitCodeAttribute(clazz: Clazz, method: Method, codeAttribute: CodeAttribute) {
            evaluator.visitCodeAttribute(clazz, method, codeAttribute)
            codeAttribute.instructionsAccept(clazz, method, this)
        }

        override fun visitAnyInstruction(
            clazz: Clazz,
            method: Method,
            codeAttribute: CodeAttribute,
            offset: Int,
            instruction: Instruction
        ) {
            if(evaluator.isTraced(offset)) {
                val beforeStack = evaluator.getStackBefore(offset)
                val stack = evaluator.getStackAfter(offset)

                if(instruction.stackPushCount(clazz) > 0) {
                    val originValue = stack.getBottomActualProducerValue(stack.size() - 1)
                    val originInsn = originValue.instructionOffsetValue().minimumValue()
                    val ctx = InstructionContext(instruction, originInsn, originValue)
                    contexts[offset] = ctx
                }

                if(instruction.stackPopCount(clazz) > 0) {
                    val originValue = beforeStack.getBottomActualProducerValue(beforeStack.size() - 1)
                    val originOffset = originValue.instructionOffsetValue().minimumValue()
                    val ctx = InstructionContext(instruction, originOffset, originValue)
                    contexts[offset] = ctx
                }
            }
        }
    }

    private class InstructionContext(val insn: Instruction, val originOffset: Int, val value: Value) {
        lateinit var origin: Instruction
    }
}