package org.spectral.asm.processor

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import org.spectral.asm.annotation.InstructionData
import java.io.File
import javax.lang.model.util.Elements
import kotlin.reflect.KClass

/**
 * Contains a grouped collection of all classes which are annotated with the 'InstructionData' annotation.
 *
 * @property qualifiedClassName String
 * @constructor
 */
class InstructionDataGroupedClasses(private val qualifiedClassName: String) {

    private val map = LinkedHashMap<String, InstructionDataAnnotatedClass>()

    @Throws(ProcessingException::class)
    operator fun plusAssign(klass: InstructionDataAnnotatedClass) {
        val existing = map[klass.name]

        /*
         * If there is a conflicting instruction name
         */
        if(existing != null) {
            throw ProcessingException(klass.annotatedClassElement, "Conflict: The class " +
            "${klass.annotatedClassElement.qualifiedName} is annotated with " +
            "@${InstructionData::class.java.simpleName} with opcode='${klass.opcode}', name='${klass.name}' " +
            "but ${existing.annotatedClassElement.qualifiedName} already uses the same instruction name.")
        }

        map[klass.name] = klass
    }

    /**
     * Generates the kotlin source enum class via kotlin-poet.
     *
     * @param elements Elements
     * @param outputDir File
     */
    fun generateCode(elements: Elements, outputDir: File) {
        val superClassName = elements.getTypeElement(qualifiedClassName)
        val instructionClassName = superClassName.simpleName.toString()
        val pkg = elements.getPackageOf(superClassName)
        val packageName = if(pkg.isUnnamed) "" else pkg.qualifiedName.toString()

        /*
         * Build the 'InstructionType.kt' ENUM kotlin class.
         */

        val instructionTypeEnum = TypeSpec.Companion.enumBuilder("InstructionType")
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("opcode", Int::class)
                    .addParameter("name", String::class)
                    .addParameter("source", KClass::class)
                    .build()
            )

        for(item in map.values) {
            instructionTypeEnum.addEnumConstant(item.name)
                .addSuperclassConstructorParameter("%L", item.opcode)
                .addSuperclassConstructorParameter("%S", item.name)
                .addSuperclassConstructorParameter("%L", item.annotatedClassElement.qualifiedName.toString())
                .build()
        }

        FileSpec.builder(packageName, "InstructionType")
            .addType(instructionTypeEnum.build())
            .build()
            .writeTo(outputDir)
    }
}