package org.spectral.matcher

import org.objectweb.asm.Attribute
import proguard.classfile.ClassPool
import proguard.classfile.Clazz
import proguard.classfile.Method
import proguard.classfile.attribute.CodeAttribute
import proguard.classfile.attribute.visitor.AllAttributeVisitor
import proguard.classfile.attribute.visitor.AttributeVisitor
import proguard.classfile.instruction.Instruction
import proguard.classfile.instruction.visitor.InstructionVisitor
import proguard.classfile.util.ClassUtil
import proguard.classfile.visitor.AllMethodVisitor
import proguard.classfile.visitor.ClassNameFilter
import proguard.classfile.visitor.MemberNameFilter
import proguard.evaluation.*
import proguard.evaluation.value.*
import java.io.IOException


object EvaluateCode {
    private const val BASIC = "-basic"
    private const val PARTICULAR = "-particular"
    private const val RANGE = "-range"
    private const val IDENTITY = "-identity"
    private const val TRACING = "-tracing"
    private const val TYPED = "-typed"
    private const val ARRAY = "-array"
    private const val DETAILEDARRAY = "-detailedarray"
    @JvmStatic
    fun main(args: Array<String>) {
        // Parse the arguments.
        var argIndex = 0
        val precision = if (args[argIndex].startsWith("-")) args[argIndex++] else "-tracing"
        val inputJarFileName = args[argIndex++]
        val classNameFilter = if (argIndex < args.size) ClassUtil.internalClassName(args[argIndex++]) else "**"
        val methodNameFilter = if (argIndex < args.size) args[argIndex++] else "*"
        try {
            // Read the program classes and library classes.
            // The latter are necessary to reconstruct the class hierarchy,
            // which is necessary to properly evaluate the code.
            // We're only reading the base jmod here. General code may need
            // additional jmod files.
            val runtimeFileName = System.getProperty("java.home") + "/jmods/java.base.jmod"
            val libraryClassPool: ClassPool = JarUtil.readJar(runtimeFileName, true)
            val programClassPool: ClassPool = JarUtil.readJar(inputJarFileName, classNameFilter, false)

            // Initialize all cross-references.
            InitializationUtil.initialize(programClassPool, libraryClassPool)

            // Create a partial evaluator for the specified precision.
            val partialEvaluator = createPartialEvaluator(precision)

            // Analyze the specified methods.
            programClassPool.classesAccept(
                ClassNameFilter(
                    classNameFilter,
                    AllMethodVisitor(
                        MemberNameFilter(
                            methodNameFilter,
                            AllAttributeVisitor(
                                MyEvaluationResultPrinter(partialEvaluator)
                            )
                        )
                    )
                )
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Creates a partial evaluator for the given precision.
     */
    private fun createPartialEvaluator(precision: String): PartialEvaluator {
        // The partial evaluator and its support classes determine
        // the precision of the analysis. You would typically pick one
        // depending on your application.

        // In this example, the value factory determines the precision.
        val valueFactory =
            if (precision == BASIC) BasicValueFactory() else if (precision == PARTICULAR) ParticularValueFactory(
                BasicValueFactory()
            ) else if (precision == RANGE) RangeValueFactory(BasicValueFactory()) else if (precision == IDENTITY) IdentifiedValueFactory() else if (precision == TRACING) ReferenceTracingValueFactory(
                BasicValueFactory()
            ) else if (precision == TYPED) TypedReferenceValueFactory() else if (precision == ARRAY) ArrayReferenceValueFactory() else if (precision == DETAILEDARRAY) DetailedArrayValueFactory() else unknownPrecision(
                precision
            )

        // In this example, we pick an invocation unit that doesn't try to
        // propagate values across fields and methods.
        val invocationUnit: InvocationUnit =
            if (precision == TRACING) ReferenceTracingInvocationUnit(BasicInvocationUnit(valueFactory)) else BasicInvocationUnit(
                valueFactory
            )

        // Create a partial evaluator with this value factory and invocation
        // unit. Don't try to evaluate unreachable code.
        return if (precision == TRACING) PartialEvaluator(
            valueFactory,
            invocationUnit,
            false,
            valueFactory as InstructionVisitor
        ) else PartialEvaluator(valueFactory, invocationUnit, false)
    }

    private fun unknownPrecision(precision: String): ValueFactory {
        throw IllegalArgumentException("Unknown precision [$precision]")
    }

    /**
     * This AttributeVisitor performs symbolic evaluation of the code of
     * each code attribute that it visits and then prints out information
     * about the its stack and local variables after each instruction.
     */
    private class MyEvaluationResultPrinter
    /**
     * Creates a new analyzer.
     * @param partialEvaluator the partial evaluator that determines the
     * precision of the analysis.
     */(private val partialEvaluator: PartialEvaluator) : AttributeVisitor,
        InstructionVisitor {
        // Implementations for AttributeVisitor.
        override fun visitAnyAttribute(clazz: Clazz?, attribute: proguard.classfile.attribute.Attribute?) {}
        override fun visitCodeAttribute(clazz: Clazz?, method: Method?, codeAttribute: CodeAttribute) {
            // Evaluate the code.
            partialEvaluator.visitCodeAttribute(clazz, method, codeAttribute)

            // Print out a table header for the instructions.
            print("Instruction | Stack")
            for (index in 0 until codeAttribute.u2maxLocals) {
                print(" | v$index")
            }
            println(" |")
            print("------------|-------|")
            for (index in 0 until codeAttribute.u2maxLocals) {
                print("----|")
            }
            println()

            // Go over all instructions to print out some information about
            // the results.
            codeAttribute.instructionsAccept(clazz, method, this)
        }

        // Implementations for InstructionVisitor.
        override fun visitAnyInstruction(
            clazz: Clazz?,
            method: Method?,
            codeAttribute: CodeAttribute?,
            offset: Int,
            instruction: Instruction
        ) {
            // Was the instruction reachable?
            if (partialEvaluator.isTraced(offset)) {
                // Print out the instruction.
                print(instruction.toString(offset).toString() + " | ")

                // Print out the stack.
                val stack = partialEvaluator.getStackAfter(offset)
                for (index in 0 until stack.size()) {
                    val actualProducerValue: Value = stack.getBottomActualProducerValue(index)
                    val producerValue: Value = stack.getBottomProducerValue(index)
                    val value: Value = stack.getBottom(index)
                    print("[" + string(actualProducerValue, producerValue, value) + "] ")
                }

                // Print out the local variables.
                val variables = partialEvaluator.getVariablesAfter(offset)
                for (index in 0 until variables.size()) {
                    val producerValue: Value? = variables.getProducerValue(index)
                    val value: Value? = variables.getValue(index)
                    print(" | " + string(null, producerValue, value))
                }
                println(" |")
            }
        }

        /**
         * Creates a readable representation of the given value and its
         * origins.
         *
         * @param actualProducerValue the original producers of the value:
         * parameters, fields, methods, "new"
         * instructions, etc (as
         * InstructionOffsetValue).
         * @param producerValue       the instructions that put the value in
         * its location on the stack or in the local
         * variables (as InstructionOffsetValue).
         * @param value               the value itself.
         */
        private fun string(
            actualProducerValue: Value?,
            producerValue: Value?,
            value: Value?
        ): String {
            val builder = StringBuilder()
            if (actualProducerValue != null) {
                builder.append(actualProducerValue)
            }
            if (producerValue != null &&
                !producerValue.equals(actualProducerValue)
            ) {
                builder.append(producerValue)
            }
            builder.append(if (value != null) value else "empty")
            return builder.toString()
        }
    }
}