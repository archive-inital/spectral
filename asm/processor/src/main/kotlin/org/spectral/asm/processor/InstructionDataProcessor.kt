package org.spectral.asm.processor

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import org.spectral.asm.annotation.InstructionData
import java.io.File
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.MirroredTypeException
import javax.lang.model.type.TypeMirror
import javax.tools.Diagnostic
import kotlin.reflect.KClass

/**
 * The primary annotation processor class which processes any '@Instruction' annotations.
 *
 * These are used to generate a 'InstructionType.kt' enum class dynamically during compile time.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_11)
@SupportedAnnotationTypes("org.spectral.asm.annotation.InstructionData")
@SupportedOptions(InstructionDataProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
class InstructionDataProcessor : AbstractProcessor() {

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment): Boolean {
        val annotatedElements = roundEnv.getElementsAnnotatedWith(InstructionData::class.java)
        if(annotatedElements.isEmpty()) return false

        val kaptKotlinGeneratedDir = processingEnv.options[KAPT_KOTLIN_GENERATED_OPTION_NAME] ?: run {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Can not find target directory for generated kotlin files.")
            return false
        }

        val enum = TypeSpec.enumBuilder("InstructionType")
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("opcode", Int::class)
                    .addParameter("insnName", String::class)
                    .addParameter("insnClass", KClass::class.asClassName().parameterizedBy(TypeVariableName("*")))
                    .build()
            )

        for(element in annotatedElements) {
            val typeElement = element.toTypeElementOrNull() ?: continue
            val annotation = element.getAnnotation(InstructionData::class.java)

            enum.addEnumConstant(annotation.name.toUpperCase(), TypeSpec.anonymousClassBuilder()
                .addSuperclassConstructorParameter("%L", annotation.opcode)
                .addSuperclassConstructorParameter("%S", annotation.name)
                .addSuperclassConstructorParameter("%T::class", ClassName("org.spectral.asm.code.instruction", typeElement.simpleName.toString()))
                .build())
        }

        enum.addProperty(PropertySpec.builder("opcode", Int::class, KModifier.PUBLIC)
                .initializer("opcode")
                .build())
            .addProperty(PropertySpec.builder("insnName", String::class, KModifier.PUBLIC)
                .initializer("insnName")
                .build())
            .addProperty(PropertySpec.builder("insnClass", KClass::class.asClassName().parameterizedBy(TypeVariableName("*")))
                .initializer("insnClass")
                .build())

        FileSpec.builder("org.spectral.asm.code", "InstructionType")
            .addType(enum.build())
            .build()
            .writeTo(File(kaptKotlinGeneratedDir))

        return true
    }

    fun Element.toTypeElementOrNull(): TypeElement? {
        if(this !is TypeElement) {
            processingEnv.messager.printMessage(Diagnostic.Kind.ERROR, "Invalid element type, class expected.", this)
            return null
        }

        return this
    }

    fun TypeElement.toMirror(): TypeMirror {
        try {
            this.asType()
        } catch (e : MirroredTypeException) {
            return e.typeMirror
        }

        throw Exception("Failed")
    }

    companion object {
        const val KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated"
    }
}