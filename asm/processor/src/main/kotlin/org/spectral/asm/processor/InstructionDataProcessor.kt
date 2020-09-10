package org.spectral.asm.processor

import com.google.auto.common.BasicAnnotationProcessor
import com.google.auto.service.AutoService
import com.google.common.collect.SetMultimap
import org.spectral.asm.annotation.InstructionData
import java.io.File
import java.io.IOException
import javax.annotation.processing.Messager
import javax.annotation.processing.Processor
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

/**
 * The primary annotation processor class which processes any '@Instruction' annotations.
 *
 * These are used to generate a 'InstructionType.kt' enum class dynamically during compile time.
 */
class InstructionDataProcessor : BasicAnnotationProcessor() {

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun getSupportedOptions(): Set<String> = setOf(KAPT_KOTLIN_GENERATED_OPTION_NAME)

    override fun initSteps(): Iterable<ProcessingStep> {
        val outputDirectory = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME]?.let { File(it) }
            ?: throw IllegalArgumentException("No output directory specified.")

        return listOf(InstructionDataProcessingStep(
            elements = processingEnv.elementUtils,
            messager = processingEnv.messager,
            outputDir = outputDirectory
        ))
    }

    companion object {
        /**
         * The kapt generated source files sourceSet name.
         */
        private const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}

/**
 * The annotation processing step that runs for each found annotation
 * requiring attention.
 *
 * @property elements Elements
 * @property messager Messager
 * @property outputDir File
 * @constructor
 */
class InstructionDataProcessingStep(
    private val elements: Elements,
    private val messager: Messager,
    private val outputDir: File
) : BasicAnnotationProcessor.ProcessingStep {

    private val instructionClasses = LinkedHashMap<String, InstructionDataGroupedClasses>()

    override fun annotations() = setOf(InstructionData::class.java)

    override fun process(elementsByAnnotation: SetMultimap<Class<out Annotation>, Element>): Set<Element> {
        try {

            /*
             * Processing Start.
             */
            for(annotatedElement in elementsByAnnotation[InstructionData::class.java]) {
                if(annotatedElement.kind != ElementKind.CLASS) {
                    throw ProcessingException(annotatedElement,
                        "Only classes can be annotated with @${InstructionData::class.java}")
                }

                val typeElement = annotatedElement as TypeElement
                val annotatedClass = InstructionDataAnnotatedClass(typeElement)

                var instructionClass = instructionClasses[annotatedClass.name]
                if(instructionClass == null) {
                    val name = annotatedClass.name
                    instructionClass = InstructionDataGroupedClasses(name)
                    instructionClasses[name] = instructionClass
                }

                instructionClass += annotatedClass

            }

            /*
             * Generate source.
             */
            for(instructionClass in instructionClasses.values) {
                instructionClass.generateCode(elements, outputDir)
            }

            instructionClasses.clear()

        } catch(e : ProcessingException) {
            error(e.element, e.message)
        } catch(e : IOException) {
            error(null, e.message)
        }

        return emptySet()
    }

    /**
     * Prints an annotation processing error to the console.
     *
     * @param e Element?
     * @param message String?
     */
    private fun error(e: Element?, message: String?) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, e)
    }
}